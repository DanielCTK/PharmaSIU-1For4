# -*- coding: utf-8 -*-
"""PharmaSIU DEMO lifecycle audit, always in-memory, no writes to database file.

Regression for the mandatory order lifecycle: CHO_XU_LY -> DA_GIU_HANG ->
SAN_SANG_NHAN -> payment recorded -> issue lots -> handover -> HOAN_TAT.
"""
import sqlite3
from pathlib import Path
DB = Path(__file__).resolve().parents[1] / 'PharmaSIU_Demo.sqlite3'

def fail_as_expected(db,sql):
    db.execute('SAVEPOINT negative')
    try:
        db.execute(sql)
    except sqlite3.IntegrityError as e:
        print('REJECTED:',str(e)[:100])
    else:
        raise AssertionError('Invalid transition accepted: '+sql)
    finally:
        db.execute('ROLLBACK TO negative')
        db.execute('RELEASE negative')

def main():
    with sqlite3.connect(f'file:{DB.as_posix()}?mode=ro',uri=True) as src:
        c=sqlite3.connect(':memory:')
        src.backup(c)
    c.execute('PRAGMA foreign_keys=ON')

    try:
        # Old state cannot be completed until payment, issue and handover.
        fail_as_expected(c,"UPDATE don_hang SET trang_thai='HOAN_TAT',giao_luc=datetime('now') WHERE don_hang_id=2")
        # A paid pickup order may issue before the handoff (not yet HOAN_TAT).
        c.execute("""UPDATE don_hang SET trang_thai='SAN_SANG_NHAN',
            trang_thai_tt='DEMO_DA_GHI_NHAN',so_tien_da_thu_vnd=72000,thanh_toan_luc=datetime('now')
            WHERE don_hang_id=2""")
        fail_as_expected(c,"UPDATE don_hang SET trang_thai='HOAN_TAT',giao_luc=datetime('now') WHERE don_hang_id=2")
        c.execute("""UPDATE chi_tiet_don_hang SET so_luong_giu=0,so_luong_xuat=2
                   WHERE don_hang_id=2 AND san_pham_id=1""")
        assert c.execute("SELECT so_luong_giu,so_luong_xuat FROM chi_tiet_don_hang WHERE don_hang_id=2").fetchone()==(0,2)
        assert c.execute("SELECT so_luong_ton FROM lo_thuoc WHERE lo_thuoc_id=1").fetchone()[0]==98, 'Issuing stock must atomically reduce physical stock'
        c.execute("""UPDATE don_hang SET giao_luc=datetime('now'),
                     trang_thai='HOAN_TAT' WHERE don_hang_id=2""")
        assert c.execute("SELECT trang_thai FROM don_hang WHERE don_hang_id=2").fetchone()[0]=='HOAN_TAT'
        fail_as_expected(c,"UPDATE don_hang SET trang_thai_tt='CHUA_THANH_TOAN' WHERE don_hang_id=2")
        fail_as_expected(c,"UPDATE chi_tiet_don_hang SET so_luong_xuat=1 WHERE don_hang_id=2")
        fail_as_expected(c,"DELETE FROM chi_tiet_don_hang WHERE don_hang_id=2")
        fail_as_expected(c,"INSERT INTO don_hang(don_hang_id,ma_don,kenh_ban,nguoi_tao_id,trang_thai) VALUES (88,'DEMO-INVALID', 'POS', 9,'HOAN_TAT')")
        # A POS sale may issue after payment even before delivery. It cannot close early.
        c.execute("""INSERT INTO don_hang (don_hang_id,ma_don,kenh_ban,nguoi_tao_id,
             trang_thai,trang_thai_tt,thanh_toan_luc)
             VALUES (89,'DEMO-POS-89','POS',9,'CHO_XU_LY',
                   'DEMO_DA_GHI_NHAN',datetime('now'))""")
        c.execute("""INSERT INTO chi_tiet_don_hang
             (chi_tiet_id,don_hang_id,san_pham_id,lo_thuoc_id,
              so_luong,don_gia_chot_vnd,so_luong_giu,so_luong_xuat)
             VALUES (890,89,2,2,1,18000,0,1)""")
        assert c.execute("SELECT so_luong_ton FROM lo_thuoc WHERE lo_thuoc_id=2").fetchone()[0]==69
        c.execute("UPDATE don_hang SET so_tien_da_thu_vnd=18000 WHERE don_hang_id=89")
        fail_as_expected(c,"UPDATE don_hang SET trang_thai='HOAN_TAT' WHERE don_hang_id=89")
        c.execute("UPDATE don_hang SET giao_luc=datetime('now'),trang_thai='HOAN_TAT' WHERE don_hang_id=89")
        print('WORKFLOW PASS: pickup payment -> issue -> handover -> completed')
        print('WORKFLOW PASS: POS payment -> issue -> handover -> completed')
        print('WORKFLOW PASS: 7 invalid mutations rejected, no production DB writes')
    finally:
        c.close()

if __name__=='__main__': main()
