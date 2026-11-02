@echo off
setlocal
cd /d "%~dp0"
where py >nul 2>&1
if %errorlevel% equ 0 (
  py -3 "tools\sync_android_sqlite.py" --once
) else (
  python "tools\sync_android_sqlite.py" --once
)
if errorlevel 1 pause
