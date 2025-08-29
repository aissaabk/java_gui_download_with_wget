
WgetGuiApp (Windows 11 Style) - Quick Start
===========================================

Requirements:
  - Windows 10/11
  - JDK 17+ (for building). To run only: JRE/JDK 17+
  - Internet access (build script will download FlatLaf jar automatically)
  - wget installed and available in PATH (the app calls "wget" via cmd)

Build & Run (No Headache):
  1) Double-click build.bat
     - It downloads FlatLaf (if missing)
     - Compiles the Java source
     - Creates a runnable JAR in dist/
     - If jpackage is available, it also creates a native EXE installer

  2) If EXE was not created, you can still run the app:
     - Double-click run.bat

Notes:
  - To enable Dark Mode, replace FlatMacLightLaf with FlatMacDarkLaf in WgetGuiApp.java
  - Icons are optional. You can add --icon app.ico to the jpackage command if you have an .ico file.
  - Ensure wget is installed (e.g., via winget: winget install GnuWin32.Wget or use aria2/curl and adapt the code)
