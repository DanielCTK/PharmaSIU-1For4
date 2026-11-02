# -*- coding: utf-8 -*-
"""Rebuild PharmaSIU six-table DEMO SQLite DB and both SQL seed scripts.
No network, credentials or real personal / patient data. Pure Python stdlib.
"""
from __future__ import annotations
import sqlite3
import sys
import datetime as dt
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TABLES = ('tai_khoan', 'san_pham', 'lo_thuoc', 'don_hang', 'chi_tiet_don_hang', 'kiem_tra_lo')
TS = '2026-09-01T08:00:00Z'
DUMMY_EMAIL = 'example.invalid'

def records():
    users = []
    for number in range(1,16):
        role = 'CUSTOMER' if number<=8 else ('CASHIER' if number<=12 else 'ADMIN')
        title = 'Khách mẫu' if role=='CUSTOMER' else ('Nhân viên mẫu' if role=='CASHIER' else 'Quản trị mẫu')
        users.append(dict(tai_khoan_id=number,
          email=f'pharma-demo-{number:02d}@{DUMMY_EMAIL}', mat_khau_hash=None,
          ho_ten=f'{title} {number:02d}', so_dien_thoai=f'000000{number:04d}',
          vai_tro=role,co_quyen_duyet_toa=False,trang_thai='DEMO',tao_luc=TS))

    raw_products = [
      ('mask-01','Khẩu trang y tế','Hộp', 'KHAC',36000,None,None),
      ('saline-01','Dung dịch NaCl 0,9%','Chai','KHAC',18000,'Natri clorid','0,9%'),
      ('cotton-01','Bông y tế','Gói','KHAC',24000,None,None),
      ('thermometer-01','Nhiệt kế điện tử','Cái','KHAC',129000,None,None),
      ('bandage-01','Băng gạc vô trùng','Gói','KHAC',29000,None,None),
      ('rx-demo-01','Thuốc kê đơn · Mẫu','Hộp','RX',0,None,None),
      ('otc-01','Sản phẩm không kê đơn · Mẫu A','Hộp','OTC',42000,None,None),
      ('otc-02','Sản phẩm không kê đơn · Mẫu B','Chai','OTC',55000,None,None),
      ('otc-03','Sản phẩm không kê đơn · Mẫu C','Gói','OTC',32000,None,None),
      ('rx-demo-02','Thuốc kê đơn · Mẫu B','Hộp','RX',0,None,None),
      ('beauty-01','Sữa rửa mặt dịu nhẹ','Chai','KHAC',118000,None,None),
      ('beauty-02','Kem dưỡng ẩm hằng ngày','Hũ','KHAC',169000,None,None),
      ('beauty-03','Son dưỡng môi','Thỏi','KHAC',59000,None,None),
      ('beauty-04','Sữa tắm dịu nhẹ','Chai','KHAC',129000,None,None),
      ('beauty-05','Dầu gội chăm sóc tóc','Chai','KHAC',145000,None,None),
    ]
    products=[]
    for index,(code,name,unit,type_,price,active,concentration) in enumerate(raw_products,1):
        products.append(dict(san_pham_id=index,ma_san_pham=code,ten_san_pham=name,
           hoat_chat=active,ham_luong=concentration,don_vi_ban=unit,
           loai_thuoc=type_,gia_niem_yet_vnd=price,dang_ban=(type_!='RX')))

    inventories = [100,70,50,12,85,20,60,60,90,10,45,40,35,22,30]
    statuses=['DAT']*15
    for idx,condition in [(6,'CHO_KIEM_TRA'),(10,'CACH_LY'),(14,'QUA_HAN'),(15,'CHO_KIEM_TRA')]:
        statuses[idx-1]=condition
    issued_by_product={1:2,3:1,7:3,9:1}
    lots=[]
    for index in range(1,16):
        stock=inventories[index-1]
        lot_date = '2025-12-31' if index==14 else f'{2027+index%2}-12-31'
        lots.append(dict(lo_thuoc_id=index,san_pham_id=index,
           so_lo_nha_sx=f'DEMO-LOT-{index:02d}',ma_chung_tu_nhap=f'DEMO-NHAP-{index:03d}',
           ten_nha_cung_cap='Nha cung cap gia lap (DEMO)',
           ngay_nhap=('2025-01-10T09:00:00Z' if index==14 else '2026-10-10T09:00:00Z'),
           han_su_dung=lot_date,vi_tri_ke=f'KE-DEMO-{index:02d}',
           so_luong_nhap=stock+issued_by_product.get(index,0),so_luong_ton=stock,tinh_trang=statuses[index-1],
           nguoi_nhap_id=13+(index%3),ghi_chu='Lo du lieu mau; khong dung lam ton kho that'))

    states=[
      'CHO_XU_LY','DA_GIU_HANG','SAN_SANG_NHAN','HOAN_TAT','DA_HUY',
      'CHO_DUYET_TOA','CHO_DUYET_TOA','DA_GIU_HANG','CHO_XU_LY','HOAN_TAT',
      'TU_CHOI','SAN_SANG_NHAN','HOAN_TAT','CHO_XU_LY','DA_GIU_HANG',
    ]
    order_channels=['WEB','WEB','APP','POS','WEB','APP','WEB','WEB','APP','POS','WEB','WEB','POS','APP','APP']
    order_user=[1,2,3,9,4,5,6,7,8,10,1,2,11,3,4]
    completed_prices={4:72000,10:158000,13:24000}
    orders=[]
    for index in range(1,16):
        status=states[index-1]
        channel=order_channels[index-1]
        pos=(channel=='POS')
        rx=index in (6,7)
        held=status in ('DA_GIU_HANG','SAN_SANG_NHAN')
        completed=status=='HOAN_TAT'
        orders.append(dict(don_hang_id=index,ma_don=f'DEMO-20261029-{index:03d}',
          kenh_ban=channel,nguoi_tao_id=order_user[index-1],
          khach_hang_id=(None if pos else order_user[index-1]),
          nhan_vien_xu_ly_id=(order_user[index-1] if pos else None),
          trang_thai=status,tao_luc=f'2026-10-{20+(index-1)//3:02d}T{8+(index%8):02d}:15:00Z',
          du_kien_nhan_luc=('2026-11-03T10:00:00Z' if held else None),
          het_han_giu_luc=('2026-11-15T23:59:00Z' if held else None),
          tep_toa_private=None,trang_thai_toa=('CHUA_GUI' if rx else 'KHONG_CAN'),
          nguoi_duyet_toa_id=None,duyet_toa_luc=None,ly_do_tu_choi_toa=None,
          phuong_thuc_tt=('TIEN_MAT_DEMO' if completed else None),
          trang_thai_tt=('DEMO_DA_GHI_NHAN' if completed else 'CHUA_THANH_TOAN'),
          so_tien_da_thu_vnd=completed_prices.get(index,0),
          thanh_toan_luc=('2026-10-28T15:30:00Z' if completed else None),
          giao_luc=('2026-10-28T15:35:00Z' if completed else None),
          ma_ca=(f'DEMO-CA-{order_user[index-1]:02d}' if pos else None),
          nguoi_doi_soat_id=None,doi_soat_luc=None,
          ghi_chu='DU LIEU DEMO; khong phai don, thanh toan hay giao thuoc that'))

    # (order_id, product_id, lot_id/NULL, quantity, hold_qty, issued_qty)
    selections=[
       (1,2,None,2,0,0),(1,3,None,1,0,0),
       (2,1,1,2,2,0),(3,2,2,1,1,0),
       (4,1,1,2,0,2),(5,3,None,1,0,0),
       (6,6,None,1,0,0),(7,10,None,1,0,0),
       (8,4,4,1,1,0),(9,5,None,2,0,0),
       (9,8,None,1,0,0),(10,7,7,3,0,3),
       (10,9,9,1,0,1),(11,8,None,1,0,0),
       (12,9,9,2,2,0),(13,3,3,1,0,1),
       (14,11,None,1,0,0),(15,12,12,1,1,0),
    ]
    lines=[]
    for index,(order,product,lot,quantity,held,issued) in enumerate(selections,1):
       lines.append(dict(chi_tiet_id=index,don_hang_id=order,
          san_pham_id=product,lo_thuoc_id=lot,so_luong=quantity,
          don_gia_chot_vnd=products[product-1]['gia_niem_yet_vnd'],
          so_luong_giu=held,so_luong_xuat=issued))

    results=[]
    for index,lot in enumerate(lots,1):
       mapping={'DAT':'DAT','CHO_KIEM_TRA':'CHO_XU_LY',
         'CACH_LY':'CACH_LY','QUA_HAN':'HET_HAN','KHONG_DAT':'KHONG_DAT'}
       results.append(dict(kiem_tra_id=index,lo_thuoc_id=index,
          nguoi_kiem_id=9+(index%4),loai_kiem='NHAP_LO' if index<=10 else 'HAN_DUNG',
          kiem_luc='2026-10-15T14:30:00Z',
          ket_qua=mapping[lot['tinh_trang']],so_luong_thuc_dem=lot['so_luong_ton'],
          ton_truoc_dieu_chinh=lot['so_luong_ton'],
          ton_sau_dieu_chinh=None,nguoi_duyet_dieu_chinh_id=None,
          duyet_dieu_chinh_luc=None,
          ghi_chu='Kiem tra mau; khong co phe duyet dieu chinh ton that'))
    return dict(tai_khoan=users,san_pham=products,lo_thuoc=lots,
        don_hang=orders,chi_tiet_don_hang=lines,kiem_tra_lo=results)

def sql_value(col,value,dialect):
    if value is None:return 'NULL'
    if isinstance(value,bool):
       return ('TRUE' if value else 'FALSE') if dialect=='postgresql' else ('1' if value else '0')
    if isinstance(value,int):return str(value)
    return "'" + str(value).replace("'","''") + "'"

def build_seed(rows,dialect):
    statements=[
       '-- Generated from tools/build_database.py; entirely fictional records.',
       '-- Account hash NULL means these are not usable login credentials.',
       'BEGIN;' if dialect=='postgresql' else 'PRAGMA foreign_keys=ON;\nBEGIN IMMEDIATE;'
    ]
    for table in TABLES:
        for row in rows[table]:
            cols=', '.join(row)
            vals=', '.join(sql_value(col,val,dialect) for col,val in row.items())
            statements.append(f'INSERT INTO {table} ({cols}) VALUES ({vals});')
    if dialect=='postgresql':
        for table in TABLES:
            statements.append(
              f"SELECT setval(pg_get_serial_sequence('public.{table}'," +
              f"'{'tai_khoan_id' if table=='tai_khoan' else 'san_pham_id' if table=='san_pham' else 'lo_thuoc_id' if table=='lo_thuoc' else 'don_hang_id' if table=='don_hang' else 'chi_tiet_id' if table=='chi_tiet_don_hang' else 'kiem_tra_id'}')," +
              f" COALESCE((SELECT MAX({'tai_khoan_id' if table=='tai_khoan' else 'san_pham_id' if table=='san_pham' else 'lo_thuoc_id' if table=='lo_thuoc' else 'don_hang_id' if table=='don_hang' else 'chi_tiet_id' if table=='chi_tiet_don_hang' else 'kiem_tra_id'}) FROM public.{table}),1), TRUE);")
    statements.append('COMMIT;')
    return '\n'.join(statements)+'\n'

def build_database():
    db=ROOT/'PharmaSIU_Demo.sqlite3'
    if db.exists() and '--force' not in sys.argv:
        raise SystemExit('Database exists. No changes made: use --force only to deliberately replace DEMO data.')
    # Build in a temporary database: a bad schema/seed must not destroy the old .sqlite3.
    staging=ROOT/'PharmaSIU_Demo.build.tmp.sqlite3'
    if staging.exists():
        raise SystemExit(f'Staging DB exists; inspect before cleanup: {staging}')
    rows=records()
    seeds={dialect:build_seed(rows,dialect) for dialect in ('sqlite','postgresql')}
    con=sqlite3.connect(staging)
    try:
        con.execute('PRAGMA foreign_keys=ON')
        con.executescript((ROOT/'sqlite/01_schema.sql').read_text(encoding='utf-8'))
        con.executescript(seeds['sqlite'])
        con.executescript((ROOT/'sqlite/03_views.sql').read_text(encoding='utf-8'))
        con.executescript((ROOT/'sqlite/04_guards.sql').read_text(encoding='utf-8'))
        integrity=con.execute('PRAGMA integrity_check').fetchone()[0]
        fk=con.execute('PRAGMA foreign_key_check').fetchall()
        counts={t:con.execute(f'SELECT COUNT(*) FROM {t}').fetchone()[0] for t in TABLES}
        if integrity != 'ok' or fk:raise ValueError((integrity,fk))
        if counts != {'tai_khoan':15,'san_pham':15,'lo_thuoc':15,
                      'don_hang':15,'chi_tiet_don_hang':18,'kiem_tra_lo':15}:
            raise ValueError(counts)
        invalid=con.execute('SELECT COUNT(*) FROM v_ton_kha_dung WHERE luong_dang_giu > ton_vat_ly OR kha_dung_du_kien < 0').fetchone()[0]
        if invalid:raise ValueError('Invalid held stock')
        con.execute('PRAGMA user_version=1')
        con.commit()
    except Exception:
        con.close()
        staging.unlink(missing_ok=True)
        raise
    else:
        con.close()
    # Atomic replace on the same volume; if Navicat holds the file open,
    # the old DB is retained and the exception is propagated.
    staging.replace(db)
    for dialect, seed_sql in seeds.items():
        path=ROOT/dialect/'02_seed.sql'
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(seed_sql,encoding='utf-8')
    print('SQLITE CREATED:',db,'bytes:',db.stat().st_size)
    print('ROWS:',counts)
    print('INTEGRITY:',integrity,'FK VIOLATIONS:',len(fk))

if __name__=='__main__': build_database()
