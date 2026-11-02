# -*- coding: utf-8 -*-
"""Populate the six-table PharmaSIU SQLite sample file with 15 products + lots.

A single shared pipe-separated CSV file is used both by this script and Android's
CatalogSeed.java. Existing nonempty user databases are NOT modified.
Usage: python database/tools/load_sample_catalog.py
"""
from __future__ import annotations
import csv
from datetime import datetime, date, timezone
from pathlib import Path
import shutil
import sqlite3

ROOT=Path(__file__).resolve().parents[2]
ASSET=ROOT/'app/src/main/assets/database/catalog_seed.csv'
MAIN=ROOT/'database/PharmaSIU_Main.sqlite3'
SCHEMA_ASSET=ROOT/'app/src/main/assets/database/pharmasiu_schema.sqlite3'

def rows():
    with ASSET.open('r',encoding='utf-8',newline='') as handle:
        records=list(csv.DictReader(handle,delimiter='|'))
    if len(records)!=15:
        raise ValueError('Expected exactly 15 product rows')
    codes=[v['code'] for v in records]
    if len(set(codes))!=len(codes):
        raise ValueError('Duplicate product codes')
    for row in records:
        if not row['name'] or not row['unit'] or int(row['price'])<1:
            raise ValueError('Invalid example product')
        if date.fromisoformat(row['expiry'])<=date.today():
            raise ValueError('Expired example lot '+row['lot'])
        if row['kind'] not in ('OTC','RX','KHAC') or row['status'] not in ('DAT','CHO_KIEM_TRA'):
            raise ValueError('Unsupported example category/status')
        if int(row['quantity'])<=0:
            raise ValueError('Invalid lot quantity')
        if row['kind']=='RX' and row['status']=='DAT':
            raise ValueError('Never mark example prescription lots as already cleared')
    return records

def load(db_file:Path=MAIN) -> None:
    records=rows()
    db_file.parent.mkdir(exist_ok=True,parents=True)
    if not db_file.exists():
        shutil.copy2(SCHEMA_ASSET,db_file)
    con=sqlite3.connect(db_file)
    try:
        con.execute('PRAGMA foreign_keys=ON')
        if con.execute('PRAGMA integrity_check').fetchone()[0]!='ok':
            raise RuntimeError('Invalid input SQLite database')
        expected={'tai_khoan','san_pham','lo_thuoc','don_hang','chi_tiet_don_hang','kiem_tra_lo'}
        tables={v[0] for v in con.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'")}
        if not expected.issubset(tables):
            raise RuntimeError('Unexpected schema, do not change this file')
        existing=con.execute('SELECT ma_san_pham FROM san_pham').fetchall()
        if existing:
            old={v[0] for v in existing}
            if len(old)==15 and old=={v['code'] for v in records}:
                print('Sample already present, no changes:',db_file)
                return
            raise RuntimeError('Database contains existing products; refusing to modify user data')
        now=datetime.now(timezone.utc).isoformat(timespec='seconds')
        with con:
            for record in records:
                con.execute('''INSERT INTO san_pham (
                  ma_san_pham,ten_san_pham,hoat_chat,ham_luong,don_vi_ban,
                  loai_thuoc,gia_niem_yet_vnd,dang_ban
                ) VALUES (?,?,?,?,?,?,?,1)''',
                (record['code'],record['name'],record['ingredient'],record['strength'],
                 record['unit'],record['kind'],int(record['price'])))
                product_id=con.execute('SELECT last_insert_rowid()').fetchone()[0]
                con.execute('''INSERT INTO lo_thuoc (
                  san_pham_id,so_lo_nha_sx,ngay_nhap,han_su_dung,
                  so_luong_nhap,so_luong_ton,tinh_trang,ghi_chu
                ) VALUES (?,?,?,?,?,?,?,?)''',
                (product_id,record['lot'],now,record['expiry'],int(record['quantity']),
                 int(record['quantity']),record['status'],
                 'Dữ liệu mẫu phục vụ kiểm thử giao diện và SQLite'))
        actual={table:con.execute(f'SELECT COUNT(*) FROM {table}').fetchone()[0]
                for table in expected}
        assert actual['san_pham']==actual['lo_thuoc']==15,actual
        assert con.execute('PRAGMA integrity_check').fetchone()[0]=='ok'
        assert not con.execute('PRAGMA foreign_key_check').fetchall()
        print('SUCCESS:',db_file)
        print('Rows:',actual,'Integrity: ok, foreign keys: ok')
    finally:
        con.close()

if __name__=='__main__':
    load()
