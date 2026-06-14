#!/bin/bash

export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
export PATH="$JAVA_HOME/bin:$PATH"

cd "$(dirname "$0")"
PROJECT_DIR="$(pwd)"

echo "============================================================"
echo " Vending Rental System - Clean build + run"
echo "============================================================"

echo ""
echo "[1/3] Running clean build (skipping tests)..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo ""
    echo "Build FAILED. Aborting startup."
    exit 1
fi

echo ""
echo "[2/3] Starting FRONTOFFICE (web app)..."
osascript -e 'tell app "Terminal" to do script "export JAVA_HOME=\"/opt/homebrew/opt/openjdk@21\"; export PATH=\"$JAVA_HOME/bin:$PATH\"; cd \"'"$PROJECT_DIR"'\" && mvn spring-boot:run"'

echo ""
echo "[3/3] Starting BACKOFFICE (JavaFX desktop)..."
osascript -e 'tell app "Terminal" to do script "export JAVA_HOME=\"/opt/homebrew/opt/openjdk@21\"; export PATH=\"$JAVA_HOME/bin:$PATH\"; cd \"'"$PROJECT_DIR"'\" && mvn javafx:run"'

echo ""
echo "Both applications are starting in separate Terminal windows."
echo "Close those windows to stop each application."
