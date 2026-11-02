# -*- coding: utf-8 -*-
"""Export a consistent DEBUG Android SQLite snapshot (including its WAL) for Navicat.

The app is force-stopped for a consistent snapshot. Requires a debuggable Android
package supporting run-as. Does not import, reset or overwrite existing databases.
"""
from pathlib import Path
import argparse,os,shutil,sqlite3,subprocess,tempfile
APP_ID='com.pharmasiu.mobile';DB_NAME='pharmasiu.db'
def main():
 parser=argparse.ArgumentParser()
 parser.add_argument('--serial',help='ADB serial')
 parser.add_argument('--output',type=Path,default=Path('PharmaSIU_Android_Export.sqlite3'))
 parser.add_argument('--adb',default=shutil.which('adb') or
   str(Path(os.environ.get('ANDROID_HOME',os.environ.get('ANDROID_SDK_ROOT','')))/
       'platform-tools'/'adb.exe'))
 args=parser.parse_args()
 base=[args.adb]+(['-s',args.serial] if args.serial else [])
 def adb(*cmd,required=True):
  result=subprocess.run(base+list(cmd),capture_output=True)
  if required and result.returncode!=0:
   raise RuntimeError(result.stderr.decode(errors='replace')[:500])
  return result
 adb('get-state')
 dest=args.output.expanduser().resolve()
 if dest.exists():raise SystemExit(f'Refusing to overwrite: {dest}')
 adb('shell','am','force-stop',APP_ID)
 with tempfile.TemporaryDirectory(prefix='pharmasiu-android-export-') as tmp:
  directory=Path(tmp);file=directory/DB_NAME
  for suffix in ('','-wal','-shm'):
   result=adb('exec-out','run-as',APP_ID,'cat','databases/'+DB_NAME+suffix,required=False)
   if result.returncode!=0:
    if not suffix:raise RuntimeError('No debuggable private SQLite DB found on this device')
    continue
   (directory/(DB_NAME+suffix)).write_bytes(result.stdout)
  if not file.read_bytes().startswith(b'SQLite format 3\x00'):
   raise RuntimeError('Invalid private database export')
  # Include the WAL in a single SQLite .backup file suitable for Navicat.
  dest.parent.mkdir(parents=True,exist_ok=True)
  source=sqlite3.connect(file)
  output=sqlite3.connect(dest)
  try:
   source.backup(output)
   assert output.execute('PRAGMA integrity_check').fetchone()[0]=='ok'
   assert output.execute('PRAGMA foreign_key_check').fetchall()==[]
   output.commit()
  except Exception:
   source.close();output.close();dest.unlink(missing_ok=True)
   raise
  else:
   source.close();output.close()
 print('Android app was stopped to snapshot safely.')
 print('Verified standalone SQLite file for Navicat:',dest)
 print('This is a snapshot, not automatic synchronization. Protect any private records.')
if __name__=='__main__':main()
