#!/bin/bash
APP_DIR="$(cd "$(dirname "$0")" && pwd)"
FLATLAF_VER=3.4.1
FLATLAF_JAR="flatlaf-$FLATLAF_VER.jar"
LIB_PATH="$APP_DIR/lib/$FLATLAF_JAR"

if [ ! -f "$LIB_PATH" ]; then
  echo "Downloading FlatLaf..."
  curl -L "https://repo1.maven.org/maven2/com/formdev/flatlaf/$FLATLAF_VER/$FLATLAF_JAR" -o "$LIB_PATH"
fi

echo
echo "============================"
echo "  Select Application to Run "
echo "============================"
echo "[1] WgetManager"
echo "[2] WgetGuiApp"
echo "[0] Exit"
echo
read -p "Enter choice: " choice

case "$choice" in
  1)
    echo "Running WgetManager..."
    java -cp "dist/WgetManager.jar:lib/$FLATLAF_JAR" WgetManager
    ;;
  2)
    echo "Running WgetGuiApp..."
    java -cp "dist/WgetGuiApp.jar:lib/$FLATLAF_JAR:resources" WgetGuiApp
    ;;
  0)
    exit 0
    ;;
  *)
    echo "Invalid choice."
    ;;
esac
