@echo off
setlocal
set FLATLAF_VER=3.4.1
set FLATLAF_JAR=flatlaf-%FLATLAF_VER%.jar
set LIB_PATH=lib\%FLATLAF_JAR%

if not exist %LIB_PATH% (
  echo Downloading FlatLaf...
  powershell -Command "Invoke-WebRequest -Uri https://repo1.maven.org/maven2/com/formdev/flatlaf/%FLATLAF_VER%/flatlaf-%FLATLAF_VER%.jar -OutFile %LIB_PATH%"
)

:MENU
echo.
echo ============================
echo   Select Application to Run
echo ============================
echo [1] WgetManager
echo [2] WgetGuiApp
echo [0] Exit
echo.
set /p choice=Enter choice: 

if "%choice%"=="1" goto RUN_MANAGER
if "%choice%"=="2" goto RUN_GUI
if "%choice%"=="0" exit /b
goto MENU

:RUN_MANAGER
echo Running WgetManager...
java -cp "dist\WgetManager.jar;lib\%FLATLAF_JAR%" WgetManager
goto END

:RUN_GUI
echo Running WgetGuiApp...
java -cp "dist\WgetGuiApp.jar;lib\%FLATLAF_JAR%;resources" WgetGuiApp
goto END

:END
pause
