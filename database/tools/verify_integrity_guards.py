from pathlib import Path
import sqlite3
root=Path(r'C:\PharmaSIU\PharmaSIU-1For4\database')
db=root/'PharmaSIU_Demo.sqlite3'
with sqlite3.connect(f'file:{db.as_posix()}?mode=ro',uri=True) as src:
  conn=sqlite3.connect(':memory:');src.backup(conn)
conn.execute('PRAGMA foreign_keys=ON')
print('GUARDS COMPILED:',conn.execute("SELECT count(*) FROM sqlite_master WHERE type='trigger'").fetchone()[0])
def fail(sql):
  conn.execute('SAVEPOINT t')
  try:conn.execute(sql)
  except sqlite3.IntegrityError as e:
    print('BLOCKED',str(e)[:85])
  else:raise AssertionError('ACCEPTED INVALID: '+sql[:100])
  finally:conn.execute('ROLLBACK TO t');conn.execute('RELEASE t')
fail("UPDATE lo_thuoc SET so_luong_ton=19 WHERE lo_thuoc_id=6")
fail("UPDATE don_hang SET trang_thai_toa='DA_DUYET' WHERE don_hang_id=6")
fail("UPDATE chi_tiet_don_hang SET don_hang_id=4 WHERE chi_tiet_id=1")
# Simulate reactivation oversubscription on a copy.
conn.execute("UPDATE don_hang SET het_han_giu_luc='2026-01-01' WHERE don_hang_id=2")
conn.execute("""INSERT INTO don_hang(don_hang_id,ma_don,kenh_ban,nguoi_tao_id,trang_thai,
  trang_thai_toa,het_han_giu_luc) VALUES
  (80,'DEMO-HOLD-80','WEB',1,'DA_GIU_HANG','KHONG_CAN','2027-01-01')""")
conn.execute("""INSERT INTO chi_tiet_don_hang(chi_tiet_id,don_hang_id,san_pham_id,
  lo_thuoc_id,so_luong,don_gia_chot_vnd,so_luong_giu,so_luong_xuat)
  VALUES(800,80,1,1,100,36000,100,0)""")
fail("UPDATE don_hang SET het_han_giu_luc='2027-01-01' WHERE don_hang_id=2")
fail("""INSERT INTO kiem_tra_lo (kiem_tra_id,lo_thuoc_id,nguoi_kiem_id,
 loai_kiem,kiem_luc,ket_qua,ton_truoc_dieu_chinh,ton_sau_dieu_chinh,
 nguoi_duyet_dieu_chinh_id,duyet_dieu_chinh_luc)
 VALUES(160,2,9,'KIEM_KE','2026-10-29T10:00Z','CHENH_LECH',70,69,1,'2026-10-29T10:00Z')""")
conn.execute("""INSERT INTO kiem_tra_lo (kiem_tra_id,lo_thuoc_id,nguoi_kiem_id,
 loai_kiem,kiem_luc,ket_qua,ton_truoc_dieu_chinh,ton_sau_dieu_chinh,
 nguoi_duyet_dieu_chinh_id,duyet_dieu_chinh_luc)
 VALUES(161,2,9,'KIEM_KE','2026-10-29T10:00Z','CHENH_LECH',70,69,13,'2026-10-29T10:00Z')""")
print('APPROVED STOCK RESULT',conn.execute('SELECT so_luong_ton FROM lo_thuoc WHERE lo_thuoc_id=2').fetchone()[0])
assert conn.execute('SELECT so_luong_ton FROM lo_thuoc WHERE lo_thuoc_id=2').fetchone()[0]==69
fail("UPDATE kiem_tra_lo SET ghi_chu='tamper' WHERE kiem_tra_id=161")
print('PASS INVARIANT GATES')
