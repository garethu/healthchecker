@echo off
setlocal enabledelayedexpansion

set REPORT=smtp_relay_discovery_report.txt
if exist "%REPORT%" del "%REPORT%"

echo SMTP Relay Discovery Report > "%REPORT%"
echo Generated: %date% %time% >> "%REPORT%"
echo ===================================== >> "%REPORT%"
echo. >> "%REPORT%"

echo [1] SMTP/MAIL/RELAY environment hints >> "%REPORT%"
set FOUND_ENV=0
for /f "delims=" %%A in ('set') do (
  echo %%A | findstr /i "SMTP MAIL RELAY" >nul
  if not errorlevel 1 (
    echo %%A >> "%REPORT%"
    set FOUND_ENV=1
  )
)
if "!FOUND_ENV!"=="0" echo No SMTP-related environment variables found. >> "%REPORT%"
echo. >> "%REPORT%"

echo [2] User principal and domain >> "%REPORT%"
for /f "delims=" %%U in ('whoami /upn') do set UPN=%%U
echo UPN: %UPN% >> "%REPORT%"

set DOMAIN=
for /f "tokens=2 delims=@" %%D in ("%UPN%") do set DOMAIN=%%D

if "%DOMAIN%"=="" (
  echo Could not extract domain from UPN. >> "%REPORT%"
  goto :fallbackDomain
)

echo Domain from UPN: %DOMAIN% >> "%REPORT%"
goto :haveDomain

:fallbackDomain
set /p DOMAIN=Enter your company email domain (example: company.com): 
echo Domain entered manually: %DOMAIN% >> "%REPORT%"

:haveDomain
echo. >> "%REPORT%"

echo [3] DNS checks >> "%REPORT%"
echo --- MX records for %DOMAIN% --- >> "%REPORT%"
nslookup -type=mx %DOMAIN% >> "%REPORT%" 2>&1
echo. >> "%REPORT%"

echo --- SRV _submission._tcp.%DOMAIN% --- >> "%REPORT%"
nslookup -type=srv _submission._tcp.%DOMAIN% >> "%REPORT%" 2>&1
echo. >> "%REPORT%"

echo --- SRV _smtp._tcp.%DOMAIN% --- >> "%REPORT%"
nslookup -type=srv _smtp._tcp.%DOMAIN% >> "%REPORT%" 2>&1
echo. >> "%REPORT%"

echo [4] Probe common relay hostnames >> "%REPORT%"
set HOSTS=smtp.%DOMAIN% relay.%DOMAIN% mail.%DOMAIN% smtp-relay.%DOMAIN% relay.mail.%DOMAIN% outbound.%DOMAIN%
for %%H in (%HOSTS%) do (
  echo Testing host: %%H >> "%REPORT%"
  powershell -NoProfile -Command "try { $a=Test-NetConnection -ComputerName '%%H' -Port 25 -WarningAction SilentlyContinue; $b=Test-NetConnection -ComputerName '%%H' -Port 587 -WarningAction SilentlyContinue; '  Port 25 : ' + $a.TcpTestSucceeded; '  Port 587: ' + $b.TcpTestSucceeded } catch { '  Error: ' + $_.Exception.Message }" >> "%REPORT%" 2>&1
  echo. >> "%REPORT%"
)

echo [5] What to ask IT (copy/paste) >> "%REPORT%"
echo Please provide approved SMTP relay for application alerts: >> "%REPORT%"
echo - Relay hostname >> "%REPORT%"
echo - Port (25/587/465) >> "%REPORT%"
echo - TLS mode (none/STARTTLS/SSL) >> "%REPORT%"
echo - Authentication required? (yes/no) >> "%REPORT%"
echo - Source IP/network allowlist required? >> "%REPORT%"
echo - Approved From address/domain >> "%REPORT%"
echo. >> "%REPORT%"

echo Done. Report written to %REPORT%
type "%REPORT%"
