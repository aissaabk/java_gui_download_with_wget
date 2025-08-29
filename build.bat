@echo off
setlocal enabledelayedexpansion

set APP_NAME=WgetManager
set FLATLAF_VER=3.4.1
set FLATLAF_JAR=flatlaf-%FLATLAF_VER%.jar
set BACKUP_DIR=backup

echo [1/7] Preparing folders...
mkdir lib 2>nul
mkdir build 2>nul
mkdir dist 2>nul
mkdir %BACKUP_DIR% 2>nul

echo [2/7] Checking Java...
where javac >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java JDK not found. Please install JDK 17+ and ensure "javac" is in PATH.
    exit /b 1
)

echo [3/7] Downloading FlatLaf v%FLATLAF_VER% (if needed)...
if not exist lib\%FLATLAF_JAR% (
    powershell -Command "Invoke-WebRequest -Uri https://repo1.maven.org/maven2/com/formdev/flatlaf/%FLATLAF_VER%/flatlaf-%FLATLAF_VER%.jar -OutFile lib\%FLATLAF_JAR%"
    if errorlevel 1 (
        echo [ERROR] Failed to download FlatLaf. Check your internet connection.
        exit /b 1
    )
)

echo [4/7] Backing up previous build if exists...
set DATETIME=%date:~10,4%-%date:~4,2%-%date:~7,2%_%time:~0,2%-%time:~3,2%-%time:~6,2%
set DATETIME=!DATETIME: =0!  REM إزالة الفراغ في الساعات

if exist dist\%APP_NAME%.jar (
    copy "dist\%APP_NAME%.jar" "%BACKUP_DIR%\%APP_NAME%_!DATETIME!.jar" >nul
    echo [INFO] Previous JAR backed up to %BACKUP_DIR%\%APP_NAME%_!DATETIME!.jar
)
if exist dist\%APP_NAME%-1.0.exe (
    copy "dist\%APP_NAME%-1.0.exe" "%BACKUP_DIR%\%APP_NAME%_!DATETIME!.exe" >nul
    echo [INFO] Previous EXE backed up to %BACKUP_DIR%\%APP_NAME%_!DATETIME!.exe
)

echo [5/7] Compiling sources...
javac -cp lib\%FLATLAF_JAR% -d build src\*.java
if errorlevel 1 (
    echo [ERROR] Compilation failed.
    exit /b 1
)

echo Main-Class: WgetManager> MANIFEST.MF
echo Class-Path: lib/%FLATLAF_JAR%>> MANIFEST.MF

echo [6/7] Creating runnable JAR...
jar cfm dist\%APP_NAME%.jar MANIFEST.MF -C build . -C resources .
if errorlevel 1 (
    echo [ERROR] JAR packaging failed.
    del MANIFEST.MF
    exit /b 1
)
del MANIFEST.MF

echo [7/7] Building native EXE with jpackage...
where jpackage >nul 2>&1
if errorlevel 1 (
    echo [WARN] jpackage not found. Skipping EXE creation. You can still run the app via run.bat
    goto runJar
)

jpackage --name %APP_NAME% --input dist --main-jar %APP_NAME%.jar --type exe --win-console
if errorlevel 1 (
    echo [WARN] jpackage failed. You can still run the app via run.bat
    goto runJar
)

echo.
echo [DONE] EXE created: %APP_NAME%\%APP_NAME%-1.0.exe (or similar in output folder)
echo.

:runJar
echo To run the JAR now, press Y. Otherwise any key to exit.
set /p CH=Run JAR? [Y/N]:
if /I "!CH!"=="Y" (
    java -cp "dist\%APP_NAME%.jar;lib\%FLATLAF_JAR%" WgetManager
) else (
    echo Bye.
)
exit /b 0
