# -*- coding: utf-8 -*-
"""Synchronize a consistent READ-ONLY snapshot of the Android app's SQLite
database to database/live/PharmaSIU_Live.sqlite3, without stopping the app.

The app's private database remains the authority. This file is a one-way snapshot:
editing the snapshot in Navicat DOES NOT change Android data. Requires a running
debuggable emulator/device + ADB run-as. Do not put real medical data in Git.
"""
from __future__ import annotations

import argparse
import hashlib
import os
from pathlib import Path
import shutil
import sqlite3
import subprocess
import sys
import tempfile
import time

PROJECT = Path(__file__).resolve().parents[2]
LIVE = PROJECT / "database" / "live" / "PharmaSIU_Live.sqlite3"
PACKAGE = "com.pharmasiu.mobile"
DEVICE_DB = "databases/pharmasiu.db"
REQUIRED_TABLES = frozenset((
    "tai_khoan", "san_pham", "lo_thuoc", "kiem_tra_lo",
    "don_hang", "chi_tiet_don_hang",
))


def adb_path(provided: str | None) -> str:
    if provided:
        return provided
    discovered = shutil.which("adb")
    if discovered:
        return discovered
    local_props = PROJECT / "local.properties"
    if local_props.exists():
        for line in local_props.read_text(encoding="utf-8").splitlines():
            if line.startswith("sdk.dir="):
                android_sdk = line.split("=", 1)[1].strip()
                android_sdk = android_sdk.replace("\\:", ":").replace("\\\\", "\\")
                candidate = Path(android_sdk) / "platform-tools" / (
                    "adb.exe" if os.name == "nt" else "adb"
                )
                if candidate.is_file():
                    return str(candidate)
    raise RuntimeError(
        "ADB not found. Install Android SDK Platform Tools or set PATH/--adb."
    )


def run_adb(adb: str, serial: str | None, *args: str,
            optional: bool = False) -> bytes | None:
    command = [adb]
    if serial:
        command += ["-s", serial]
    command += list(args)
    result = subprocess.run(command, stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE, timeout=20)
    if result.returncode != 0:
        if optional:
            return None
        details = result.stderr.decode("utf-8", errors="replace").strip()
        raise RuntimeError(details[:250] or "ADB command failed")
    return result.stdout


def connected_device(adb: str, serial: str | None) -> str:
    output = run_adb(adb, None, "devices").decode(errors="replace")
    connected = [
        line.split()[0] for line in output.splitlines()[1:]
        if len(line.split()) >= 2 and line.split()[1] == "device"
    ]
    if serial:
        if serial not in connected:
            raise RuntimeError(f"Android device {serial} is not connected")
        return serial
    if not connected:
        raise RuntimeError("No Android emulator/device connected via ADB")
    if len(connected) != 1:
        raise RuntimeError(
            "Multiple Android devices found; pass --serial to select one"
        )
    return connected[0]


def make_consistent_snapshot(adb: str, serial: str, work: Path) -> Path:
    base = work / "pharmasiu.db"
    for ext in ("", "-wal", "-shm"):
        data = run_adb(
            adb, serial, "exec-out", "run-as", PACKAGE,
            "cat", DEVICE_DB + ext, optional=bool(ext)
        )
        if data is None:
            if not ext:
                raise RuntimeError(
                    "Cannot access app database (need installed debug app "
                    "and initial launch)."
                )
            continue
        (work / ("pharmasiu.db" + ext)).write_bytes(data)
    if not base.is_file() or base.read_bytes()[:16] != b"SQLite format 3\x00":
        raise RuntimeError("Invalid SQLite header from Android")

    snapshot = work / "verified.sqlite3"
    source = sqlite3.connect(str(base), timeout=2.0)
    target = sqlite3.connect(str(snapshot), timeout=2.0)
    try:
        source.backup(target, pages=64, sleep=0.01)
        result = target.execute("PRAGMA integrity_check").fetchone()[0]
        if result != "ok":
            raise RuntimeError(f"SQLite integrity check: {result}")
        if target.execute("PRAGMA foreign_key_check").fetchone():
            raise RuntimeError("Foreign key inconsistency in Android database")
        tables = {
            row[0] for row in target.execute(
                "SELECT name FROM sqlite_master WHERE type='table' "
                "AND name NOT LIKE 'sqlite_%' AND name != 'android_metadata'"
            )
        }
        if tables != REQUIRED_TABLES:
            raise RuntimeError(f"Unexpected app database schema: {sorted(tables)}")
        target.commit()
    finally:
        source.close()
        target.close()
    return snapshot


def one_sync(adb: str, serial: str, previous_digest: bytes | None = None
             ) -> tuple[bool, bytes]:
    LIVE.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(
        prefix=".sync_tmp_", dir=str(LIVE.parent)
    ) as temporary:
        temp = Path(temporary)
        # A busy app may change WAL while the three ADB reads run.
        # Verify a snapshot before publishing; retry the source read when invalid.
        for attempt in range(3):
            try:
                for suffix in ("pharmasiu.db", "pharmasiu.db-wal", "pharmasiu.db-shm",
                               "verified.sqlite3"):
                    (temp / suffix).unlink(missing_ok=True)
                snapshot = make_consistent_snapshot(adb, serial, temp)
                break
            except (sqlite3.DatabaseError, RuntimeError):
                if attempt == 2:
                    raise
                time.sleep(0.25)
        digest = hashlib.sha256(snapshot.read_bytes()).digest()
        if previous_digest == digest and LIVE.is_file():
            return False, digest
        if previous_digest is None and LIVE.is_file():
            if hashlib.sha256(LIVE.read_bytes()).digest() == digest:
                return False, digest
        if not LIVE.exists():
            os.replace(snapshot, LIVE)
            return True, digest

        # SQLite online backup uses transactions and updates the EXISTING path,
        # rather than renaming over a file Navicat may have open on Windows.
        # It also preserves a stable connection pathname for Navicat.
        reader = sqlite3.connect(f"file:{snapshot.as_posix()}?mode=ro", uri=True)
        writer = sqlite3.connect(str(LIVE), timeout=3)
        try:
            reader.backup(writer, pages=64, sleep=0.02)
            if writer.execute("PRAGMA integrity_check").fetchone()[0] != "ok":
                raise RuntimeError("Post-sync SQLite integrity check failed")
            writer.commit()
        except sqlite3.Error as exc:
            raise RuntimeError(
                "Snapshot currently busy in Navicat. End editing/refresh and retry."
            ) from exc
        finally:
            reader.close()
            writer.close()
        return True, digest


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--once", action="store_true",
                        help="Export once and exit (useful before opening Navicat)")
    parser.add_argument("--interval", type=int, default=10,
                        help="Seconds between checks during auto-sync (minimum 5)")
    parser.add_argument("--serial",
                        help="Emulator serial, e.g. emulator-5554")
    parser.add_argument("--adb", help="Path to adb.exe; auto-detected by default")
    args = parser.parse_args()
    if args.interval < 5:
        parser.error("--interval must be >= 5 seconds")

    adb = adb_path(args.adb)
    print("PharmaSIU Android SQLite -> Windows (ONE WAY, READ-ONLY snapshot)")
    print("Destination:", LIVE)
    print("Android is the source of truth; do not edit the snapshot in Navicat.")
    print("Auto-sync works ONLY while this process and emulator/device are running.")
    print("Press Ctrl+C to stop.", flush=True)
    last_error = None
    last_digest = None
    try:
        while True:
            try:
                serial = connected_device(adb, args.serial)
                changed, last_digest = one_sync(adb, serial, last_digest)
                if changed:
                    print(time.strftime("%H:%M:%S"), "Snapshot updated.", flush=True)
                elif args.once:
                    print("Snapshot already current.", flush=True)
                last_error = None
            except (OSError, RuntimeError, sqlite3.Error, subprocess.TimeoutExpired) as exc:
                message = str(exc)
                if message != last_error:
                    print(time.strftime("%H:%M:%S"), "Sync unavailable:", message,
                          file=sys.stderr, flush=True)
                    last_error = message
                if args.once:
                    return 1
            if args.once:
                return 0
            time.sleep(args.interval)
    except KeyboardInterrupt:
        print("\nSQLite auto-sync stopped.")
        return 0


if __name__ == "__main__":
    sys.exit(main())
