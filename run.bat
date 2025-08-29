@echo off
setlocal
set APP_NAME=WgetGuiApp
set FLATLAF_VER=3.4.1
set FLATLAF_JAR=flatlaf-%FLATLAF_VER%.jar

where java >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Java Runtime not found. Please install JRE/JDK 17+.
  exit /b 1
)

if not exist lib\%FLATLAF_JAR% (
  echo Downloading FlatLaf...
  powershell -Command "Invoke-WebRequest -Uri https://repo1.maven.org/maven2/com/formdev/flatlaf/%FLATLAF_VER%/flatlaf-%FLATLAF_VER%.jar -OutFile lib\%FLATLAF_JAR%"
)

echo Running %APP_NAME%...
java -cp "dist\%APP_NAME%.jar;lib\%FLATLAF_JAR%" WgetGuiApp
