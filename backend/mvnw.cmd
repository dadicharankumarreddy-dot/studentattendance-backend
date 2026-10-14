@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Script for Windows CMD / PowerShell
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "MVN_EXEC="

@REM Check if mvn is on PATH
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    set "MVN_EXEC=mvn"
)

@REM Check user .m2 maven installation
if not defined MVN_EXEC (
    if exist "%USERPROFILE%\.m2\maven\bin\mvn.cmd" (
        set "MVN_EXEC=%USERPROFILE%\.m2\maven\bin\mvn.cmd"
    )
)

@REM Check standard Program Files installations
if not defined MVN_EXEC (
    for /d %%D in ("%ProgramFiles%\apache-maven*" "%ProgramFiles(x86)%\apache-maven*") do (
        if exist "%%D\bin\mvn.cmd" set "MVN_EXEC=%%D\bin\mvn.cmd"
    )
)

if not defined MVN_EXEC (
    echo Error: Maven executable not found. Please install Maven or add it to PATH.
    exit /b 1
)

call "%MVN_EXEC%" %*
exit /b %ERRORLEVEL%
