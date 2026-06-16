@echo off
REM WorldShare Mod - Automatisches Build-Skript für Windows

echo 🌐 WorldShare Mod Builder
echo ==========================
echo.

REM Prüfe ob gradlew.bat existiert
if not exist "gradlew.bat" (
    echo ❌ Fehler: gradlew.bat nicht gefunden!
    echo Du musst im Hauptverzeichnis sein (wo build.gradle ist)
    exit /b 1
)

echo ✅ Starte Build...
echo.

REM Führe Gradle aus
call gradlew.bat clean build

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅✅✅ BUILD ERFOLGREICH! ✅✅✅
    echo.
    echo 📦 Deine JAR-Datei:
    echo build\libs\worldshare-1.0.0.jar
    echo.
    echo Kopiere diese Datei in deinen Minecraft mods\ Ordner!
) else (
    echo.
    echo ❌ Build fehlgeschlagen!
    exit /b 1
)

pause
