# -*- coding: utf-8 -*-
"""Provision a LOCAL Android SQLite asset without any hard-coded credentials.

Before installing the app the first time:
python database/tools/provision_account.py --role ADMIN --email admin@mycompany.vn --name "Quản trị nội bộ"
Use a secure password entered interactively. Existing devices won't be overwritten.
"""
from pathlib import Path
import argparse,base64,getpass,hashlib,sqlite3,secrets,sys
DEFAULT=Path(__file__).resolve().parents[2]/'app/src/main/assets/database/pharmasiu_schema.sqlite3'
def password_hash(password):
 if not (8<=len(password)<=128):raise ValueError('Password must contain 8-128 characters')
 salt=secrets.token_bytes(16)
 rounds=210000
 digest=hashlib.pbkdf2_hmac('sha256',password.encode('utf-8'),salt,rounds,dklen=32)
 b64=lambda b:base64.b64encode(b).decode('ascii').rstrip('=')
 return f'pbkdf2_sha256${rounds}${b64(salt)}${b64(digest)}'
def provision(path,email,name,role,password,phone=None):
 if role not in ('ADMIN','CASHIER','CUSTOMER'):raise ValueError('Invalid role')
 if not path.is_file():raise FileNotFoundError(path)
 if '@' not in email or not name.strip():raise ValueError('Email and name required')
 if phone is not None and (not phone.startswith('0') or not phone.isdigit() or len(phone) not in (10,11)):
  raise ValueError('Invalid phone')
 conn=sqlite3.connect(path)
 try:
  conn.execute('PRAGMA foreign_keys=ON')
  valid=conn.execute('PRAGMA integrity_check').fetchone()[0]
  if valid!='ok':raise ValueError('Invalid DB '+str(valid))
  with conn:
   conn.execute("""INSERT INTO tai_khoan
    (email,mat_khau_hash,ho_ten,so_dien_thoai,vai_tro,co_quyen_duyet_toa,trang_thai)
    VALUES(?,?,?,?,?,0,'HOAT_DONG')""",
    (email.strip().lower(),password_hash(password),name.strip(),phone,role))
 finally:conn.close()
def main():
 parser=argparse.ArgumentParser(description='Provision one account to a local PharmaSIU DB')
 parser.add_argument('--database',type=Path,default=DEFAULT)
 parser.add_argument('--role',choices=('ADMIN','CASHIER','CUSTOMER'),required=True)
 parser.add_argument('--email',required=True)
 parser.add_argument('--name',required=True)
 parser.add_argument('--phone')
 args=parser.parse_args()
 if not sys.stdin.isatty():
  raise SystemExit('Interactive terminal required for safe password input; never pass password on CLI')
 password=getpass.getpass('New password (not echoed): ')
 confirmation=getpass.getpass('Confirm password: ')
 if password!=confirmation:raise SystemExit('Passwords differ; no change made')
 provision(args.database,args.email,args.name,args.role,password,args.phone)
 print('Account provisioned in',args.database,'role=',args.role,'(password not shown)')
 print('Rebuild/reinstall on a NEW app data directory to use modified asset.')
if __name__=='__main__':main()
