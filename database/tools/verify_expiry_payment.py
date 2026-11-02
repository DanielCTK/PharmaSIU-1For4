# -*- coding: utf-8 -*-
"""Adversarial expiry and paid-amount tests on a RAM copy of PharmaSIU_Demo.sqlite3."""
import sqlite3
from pathlib import Path
DB=Path(__file__).resolve().parents[1]/'PharmaSIU_Demo.sqlite3'
def blocked(c,sql,fragment):
 c.execute('SAVEPOINT tx')
 try:c.execute(sql)
 except sqlite3.IntegrityError as e:
  assert fragment in str(e),(fragment,str(e))
  print('REJECTED',str(e))
 else:
  raise AssertionError('accepted invalid operation: '+sql)
 finally:
  c.execute('ROLLBACK TO tx');c.execute('RELEASE tx')

def main():
 with sqlite3.connect(f'file:{DB.as_posix()}?mode=ro',uri=True) as src:
  c=sqlite3.connect(':memory:');src.backup(c)
 c.execute('PRAGMA foreign_keys=ON')
 try:
  # Expired physical lot cannot be issued even if paid-at was in the past.
  c.execute('UPDATE chi_tiet_don_hang SET so_luong_giu=0 WHERE chi_tiet_id=3')
  c.execute("UPDATE lo_thuoc SET han_su_dung='2026-10-01' WHERE lo_thuoc_id=1")
  c.execute("""UPDATE don_hang SET trang_thai='SAN_SANG_NHAN',
    trang_thai_tt='DEMO_DA_GHI_NHAN',
    thanh_toan_luc='2026-01-01T10:00:00Z',so_tien_da_thu_vnd=72000
    WHERE don_hang_id=2""")
  blocked(c,"UPDATE chi_tiet_don_hang SET so_luong_xuat=2 WHERE chi_tiet_id=3",
          'INVALID_ISSUE_UPDATE')
  # Restore lot quality for payment and post-issue checks.
  c.execute("UPDATE lo_thuoc SET han_su_dung='2028-12-31' WHERE lo_thuoc_id=1")
  c.execute('UPDATE don_hang SET so_tien_da_thu_vnd=0 WHERE don_hang_id=2')
  c.execute('UPDATE chi_tiet_don_hang SET so_luong_xuat=2 WHERE chi_tiet_id=3')
  blocked(c,"UPDATE chi_tiet_don_hang SET so_luong_xuat=1 WHERE chi_tiet_id=3",
          'ISSUED_QUANTITY_IMMUTABLE')
  blocked(c,"UPDATE don_hang SET giao_luc=datetime('now'),trang_thai='HOAN_TAT' WHERE don_hang_id=2",
          'INCOMPLETE_ISSUE_OR_HANDOFF')
  c.execute('UPDATE don_hang SET so_tien_da_thu_vnd=72000 WHERE don_hang_id=2')
  c.execute("UPDATE don_hang SET giao_luc=datetime('now'),trang_thai='HOAN_TAT' WHERE don_hang_id=2")
  print('PASS: current expiry, non-reversible issue, recorded amount before HOAN_TAT')
 finally:c.close()
if __name__=='__main__':main()
