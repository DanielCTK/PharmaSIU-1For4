# -*- coding: utf-8 -*-
"""Read-only integrity and adversarial integrity tests for PharmaSIU demo DB.

Uses an in-memory copy for mutations, never modifies the user's .sqlite3 file.
Usage: python database/tools/verify_database.py
"""
from pathlib import Path
import sqlite3

DB = Path(__file__).resolve().parents[1] / 'PharmaSIU_Demo.sqlite3'
EXPECTED = {
  'tai_khoan':15, 'san_pham':15, 'lo_thuoc':15,
  'don_hang':15, 'chi_tiet_don_hang':18, 'kiem_tra_lo':15,
}

def expect_blocked(con,statement,params=()):
    con.execute('SAVEPOINT negative_test')
    try:
        con.execute(statement,params)
    except sqlite3.IntegrityError as exc:
        con.execute('ROLLBACK TO negative_test')
        con.execute('RELEASE negative_test')
        print('BLOCKED as expected:',str(exc)[:100])
    else:
        con.execute('ROLLBACK TO negative_test')
        con.execute('RELEASE negative_test')
        raise AssertionError('Database wrongly accepted an invalid operation')

def main():
    if not DB.is_file(): raise SystemExit(f'Not found: {DB}')
    with sqlite3.connect(f'file:{DB.as_posix()}?mode=ro',uri=True) as source:
        source.execute('PRAGMA foreign_keys=ON')
        assert source.execute('PRAGMA integrity_check').fetchone()[0]=='ok'
        assert not source.execute('PRAGMA foreign_key_check').fetchall()
        tables={r[0] for r in source.execute(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'")}
        assert tables==set(EXPECTED),tables
        # Exact source-diagram columns, no extra/unrelated physical tables.
        expected_column_counts = {"tai_khoan":9,"san_pham":9,"lo_thuoc":13,
            "don_hang":24,"chi_tiet_don_hang":8,"kiem_tra_lo":12}
        for table,count in expected_column_counts.items():
            cols=source.execute("PRAGMA table_info("+table+")").fetchall()
            assert len(cols)==count,(table,len(cols),count)
        actual={table:source.execute(f'SELECT COUNT(*) FROM {table}').fetchone()[0]
                for table in EXPECTED}
        assert actual==EXPECTED,actual
        views={r[0] for r in source.execute("SELECT name FROM sqlite_master WHERE type='view'")}
        assert views=={
           'v_ton_kha_dung','v_tong_gia_tri_don',
           'v_canh_bao_lo','v_doi_soat_ca_demo'},views
        assert source.execute("SELECT COUNT(*) FROM tai_khoan WHERE mat_khau_hash IS NOT NULL").fetchone()[0]==0
        assert source.execute("""SELECT COUNT(*) FROM chi_tiet_don_hang c
            JOIN san_pham p ON p.san_pham_id=c.san_pham_id
            WHERE p.loai_thuoc='RX' AND (c.so_luong_giu>0 OR c.so_luong_xuat>0)""").fetchone()[0]==0
        # A held item does not remove physical stock; issued units explain the seeded inventory delta.
        stock_delta=source.execute("""SELECT COUNT(*) FROM lo_thuoc l WHERE
          l.so_luong_nhap - l.so_luong_ton <> COALESCE((
            SELECT SUM(c.so_luong_xuat) FROM chi_tiet_don_hang c
            WHERE c.lo_thuoc_id=l.lo_thuoc_id),0)""").fetchone()[0]
        assert stock_delta==0,stock_delta
        sale_mismatch=source.execute("""SELECT COUNT(*) FROM don_hang d
          JOIN v_tong_gia_tri_don v ON v.don_hang_id=d.don_hang_id
          WHERE d.kenh_ban='POS' AND d.trang_thai='HOAN_TAT'
            AND d.so_tien_da_thu_vnd<>v.tong_gia_tri_vnd""").fetchone()[0]
        assert sale_mismatch==0,sale_mismatch
        # Line prices are a historical snapshot, not recalculated from the catalog.
        assert source.execute("""SELECT tong_gia_tri_vnd FROM v_tong_gia_tri_don
          WHERE don_hang_id=10""").fetchone()[0]==158000
        print('READ ONLY:',actual,'views:',len(views),'foreign_key_check: ok')

        temp=sqlite3.connect(':memory:')
        try:
            source.backup(temp)
            temp.execute('PRAGMA foreign_keys=ON')
            assert temp.execute('PRAGMA foreign_keys').fetchone()[0]==1
            temp.execute("UPDATE don_hang SET het_han_giu_luc=datetime('now','+30 days') WHERE don_hang_id=2")
            # Wrong product/lot: composite FK must reject even when both IDs separately exist.
            expect_blocked(temp,"""INSERT INTO chi_tiet_don_hang
              (chi_tiet_id,don_hang_id,san_pham_id,lo_thuoc_id,so_luong,
               don_gia_chot_vnd,so_luong_giu,so_luong_xuat)
              VALUES (9001,2,2,1,1,18000,0,0)""")
            expect_blocked(temp,"""INSERT INTO chi_tiet_don_hang
              (chi_tiet_id,don_hang_id,san_pham_id,lo_thuoc_id,so_luong,
               don_gia_chot_vnd,so_luong_giu,so_luong_xuat)
              VALUES (9002,2,1,1,999,36000,999,0)""")
            temp.execute("UPDATE don_hang SET trang_thai='DA_GIU_HANG',het_han_giu_luc=datetime('now','+30 days') WHERE don_hang_id=6")
            temp.execute("UPDATE lo_thuoc SET tinh_trang='DAT' WHERE lo_thuoc_id=6")
            expect_blocked(temp,"""INSERT INTO chi_tiet_don_hang
              (chi_tiet_id,don_hang_id,san_pham_id,lo_thuoc_id,so_luong,
               don_gia_chot_vnd,so_luong_giu,so_luong_xuat)
              VALUES (9003,6,6,6,1,0,1,0)""")
            expect_blocked(temp,"UPDATE don_hang SET trang_thai='DA_HUY' WHERE don_hang_id=2")
            expect_blocked(temp,"UPDATE lo_thuoc SET so_luong_ton=1 WHERE lo_thuoc_id=1")
            expect_blocked(temp,"UPDATE lo_thuoc SET tinh_trang='CACH_LY' WHERE lo_thuoc_id=1")
            # No issue/sale on an unconfirmed order; no Rx issue without authorized approval.
            expect_blocked(temp,"""INSERT INTO chi_tiet_don_hang
              (chi_tiet_id,don_hang_id,san_pham_id,lo_thuoc_id,so_luong,
               don_gia_chot_vnd,so_luong_giu,so_luong_xuat)
              VALUES (9005,1,2,2,1,18000,0,1)""")
            temp.execute("UPDATE san_pham SET dang_ban=1 WHERE san_pham_id=6")
            temp.execute("""UPDATE don_hang SET trang_thai='SAN_SANG_NHAN',
              trang_thai_tt='DEMO_DA_GHI_NHAN',thanh_toan_luc=datetime('now'),
              giao_luc=datetime('now') WHERE don_hang_id=6""")
            expect_blocked(temp,"""INSERT INTO chi_tiet_don_hang
              (chi_tiet_id,don_hang_id,san_pham_id,lo_thuoc_id,so_luong,
               don_gia_chot_vnd,so_luong_giu,so_luong_xuat)
              VALUES (9006,6,6,6,1,0,0,1)""")
            expect_blocked(temp,"UPDATE don_hang SET trang_thai='CHO_XU_LY' WHERE don_hang_id=4")
            # A new available held line is permitted, but must NOT change the original DB.
            temp.execute("""INSERT INTO chi_tiet_don_hang
              (chi_tiet_id,don_hang_id,san_pham_id,lo_thuoc_id,so_luong,
               don_gia_chot_vnd,so_luong_giu,so_luong_xuat)
              VALUES (9004,2,1,1,1,36000,1,0)""")
            assert temp.execute("SELECT luong_dang_giu FROM v_ton_kha_dung WHERE lo_thuoc_id=1").fetchone()[0]==3
            print('NEGATIVE TESTS: 9/9 blocked; valid hold: passed')
        finally:
            temp.close()
    print('ALL PHARMASIU DATABASE CHECKS PASSED. Original DB unchanged.')
if __name__=='__main__':main()
