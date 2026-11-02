@echo off
setlocal
cd /d "%~dp0"
where py >nul 2>&1
if %errorlevel% equ 0 (
  py -3 "tools\sync_android_sqlite.py" --interval 10
) else (
  python "tools\sync_android_sqlite.py" --interval 10
)
if errorlevel 1 (
  echo.
  echo Unable to start SQLite sync. Check Python 3 and Android emulator.
  pause
)
