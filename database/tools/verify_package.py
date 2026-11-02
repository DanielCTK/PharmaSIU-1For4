# -*- coding: utf-8 -*-
"""Check exact byte parity of downloadable ZIP against latest on-disk PharmaSIU DB."""
from pathlib import Path
import zipfile, sqlite3

ROOT=Path(__file__).resolve().parents[1]
ZIP=ROOT/'PharmaSIU_Database_6Tables.zip'
def main():
    assert ZIP.exists(),f'Not packaged yet: {ZIP}'
    candidates=[
        p for p in ROOT.rglob('*') if p.is_file()
        and p!=ZIP and '__pycache__' not in p.parts
        and p.suffix not in ('.pyc','.shm','.wal')
    ]
    expected={'database/'+p.relative_to(ROOT).as_posix() for p in candidates}
    with zipfile.ZipFile(ZIP,'r') as z:
        assert z.testzip() is None,'ZIP CRC mismatch'
        found=set(z.namelist())
        assert found==expected,{'missing':sorted(expected-found),'extra':sorted(found-expected)}
        for p in candidates:
            arcname='database/'+p.relative_to(ROOT).as_posix()
            assert z.read(arcname)==p.read_bytes(),f'Stale ZIP payload {arcname}'
        data=z.read('database/PharmaSIU_Demo.sqlite3')
    mem=sqlite3.connect(':memory:')
    try:
        mem.deserialize(data)
        assert mem.execute('PRAGMA integrity_check').fetchone()[0]=='ok'
        assert mem.execute('PRAGMA foreign_key_check').fetchall()==[]
        tables=[r[0] for r in mem.execute("""SELECT name FROM sqlite_master
            WHERE type='table' AND name NOT LIKE 'sqlite_%'""")]
        assert len(tables)==6,tables
        triggers=mem.execute("SELECT COUNT(*) FROM sqlite_master WHERE type='trigger'").fetchone()[0]
        assert triggers>=29,triggers
        assert sum(mem.execute(f'SELECT COUNT(*) FROM {t}').fetchone()[0] for t in tables)==93
        print('PACKAGE PASS',len(candidates),'files',len(data),'database bytes',
              triggers,'active SQLite triggers','all bytes identical')
    finally:mem.close()
if __name__=='__main__':main()
