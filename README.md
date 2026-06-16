# WorldShare - Minecraft Fabric Mod

🌐 Teile deine Singleplayer-Welt mit einem Code - Ohne komplizierte Einrichtung!

## Features

✨ **Einfaches Sharing**
- Generieren Sie einen kurzen, lesbaren Code (z.B. A3F-92X)
- Freunde können mit dem Code der Welt beitreten
- Keine Port-Weiterleitung, keine komplexe Konfiguration nötig

🚀 **Automatische Installation**
- playit.gg wird automatisch heruntergeladen und installiert
- Funktioniert auf Windows, Linux und macOS
- Unterstützt auch Cracked-Spieler (online-mode=false)

⏱️ **Sechsstunden-Sessions**
- Codes laufen nach 6 Stunden automatisch ab
- Neue Codes können beliebig oft generiert werden

🔒 **Datenschutz**
- Keine Benutzerkonten erforderlich
- Deine Welt wird nur geteilt, wenn du einen Code generierst
- Einfach den Code löschen und der Zugriff wird blockiert

## Installation

### Anforderungen

- Minecraft 1.21.1
- Fabric Loader
- Java 21

### Schritte

1. **Lade die Mod herunter**
   ```bash
   git clone https://github.com/jdq2dh85yb-svg/worldshare-mod.git
   cd worldshare-mod
   ```

2. **Baue die Mod**
   ```bash
   ./gradlew build
   ```
   
   Oder auf Windows:
   ```bash
   gradlew.bat build
   ```

3. **Installiere die Mod**
   - Kopiere `build/libs/worldshare-*.jar` in dein `mods/` Verzeichnis
   - Starte Minecraft mit dem Fabric Loader

## Verwendung

### Host World (Welt freigeben)

1. Öffne deine Singleplayer-Welt
2. Drücke **U** um das WorldShare Menü zu öffnen
3. Klicke auf **"📡 Host World"**
4. Warte bis der Code generiert wird
5. Teile den Code **"A3F-92X"** mit deinem Freund
6. Klicke **"📋 Code kopieren"** um den Code in die Zwischenablage zu kopieren
7. Klicke **"⏹ Hosting beenden"** um das Sharing zu stoppen

### Join World (Freund-Welt beitreten)

1. Drücke **U** um das WorldShare Menü zu öffnen
2. Klicke auf **"🔗 Join World"**
3. Gib den Code ein (z.B. **A3F-92X**)
4. Klicke **"🔗 Beitreten"**
5. Minecraft verbindet sich automatisch!

## Relay-Server Deployment

Die Mod benötigt einen Relay-Server um Codes zu speichern und aufzulösen.

### Schneller Deploy auf Render.com (kostenlos)

1. Gehe zu https://render.com
2. Melde dich mit GitHub an
3. Klicke **"New +" → "Web Service"**
4. Verbinde das `worldshare-mod` Repository
5. Setze den **Build Command** auf `npm install`
6. Setze den **Start Command** auf `npm start`
7. Wähle den kostenlosen Plan
8. Klicke **"Create Web Service"**

Nach 2-3 Minuten bekommst du eine URL wie:
```
https://worldshare-relay-xxxx.onrender.com
```

Aktualisiere dann in der Mod die `RelayServerClient.java`:
```java
private static final String RELAY_SERVER_URL = "https://worldshare-relay-xxxx.onrender.com";
```

Mehr Details im [Relay-Server README](relay-server/README.md)

## Technische Details

### Mod-Struktur

```
src/main/java/de/worldshare/
├── WorldShareClient.java          # Entry Point
├── screen/
│   ├── WorldShareMenuScreen.java  # Hauptmenü
│   ├── HostWorldScreen.java       # Host-Bildschirm
│   └── JoinWorldScreen.java       # Join-Bildschirm
└── util/
    ├── PlayitDownloader.java      # playit.gg Management
    └── RelayServerClient.java     # API Client
```

### Abhängigkeiten

- **Minecraft 1.21.1**
- **Fabric API 0.103.0+1.21.1**
- **playit.gg CLI** (wird automatisch heruntergeladen)

### Build-System

- **Gradle 8.8+**
- **Java 21**

## Entwicklung

### Setup

```bash
# Klone das Repo
git clone https://github.com/jdq2dh85yb-svg/worldshare-mod.git
cd worldshare-mod

# Lade Abhängigkeiten
./gradlew

# Baue die Mod
./gradlew build

# Starte das Entwicklungs-Environment
./gradlew runClient
```

### Struktur

```
worldshare-mod/
├── src/main/java/de/worldshare/    # Mod Source Code
├── relay-server/                    # Node.js Relay Server
├── build.gradle                     # Gradle Konfiguration
├── gradle.properties                # Version & Properties
├── fabric.mod.json                  # Mod Manifest
└── README.md                        # Diese Datei
```

## Fehlerbehebung

### "playit.gg Download fehlgeschlagen"
- Prüfe deine Internetverbindung
- Stelle sicher dass die `mods/worldshare/` Verzeichnis beschreibbar ist

### "Code nicht gefunden"
- Prüfe dass der Code korrekt eingegeben wurde
- Codes laufen nach 6 Stunden ab
- Der Relay-Server muss online sein

### "Kann nicht als Host fungieren"
- Du musst eine Singleplayer-Welt offen haben
- Prüfe dass du Operator-Rechte hast

### "Connection refused"
- Der Relay-Server läuft nicht
- Prüfe die Relay-Server URL in `RelayServerClient.java`
- Überprüfe deine Firewall-Einstellungen

## API Referenz

### Relay-Server Endpoints

```
POST /publish
  Veröffentlicht einen Code
  Body: { "code": "A3F-92X", "address": "host:port" }

GET /resolve?code=A3F-92X
  Löst einen Code auf
  Response: { "code": "A3F-92X", "address": "host:port" }

DELETE /remove?code=A3F-92X
  Entfernt einen Code

GET /stats
  Gibt Server-Statistiken zurück

GET /health
  Health Check
```

Mehr Details im [Relay-Server README](relay-server/README.md)

## Lizenz

MIT License - siehe [LICENSE](LICENSE) Datei

## Beiträge

Beiträge sind willkommen! Bitte erstelle einen Fork und einen Pull Request.

## Support

- 📝 [GitHub Issues](https://github.com/jdq2dh85yb-svg/worldshare-mod/issues)
- 💬 [Diskussionen](https://github.com/jdq2dh85yb-svg/worldshare-mod/discussions)

---

**Entwickelt mit ❤️ für Minecraft Spieler**

Viel Spaß beim Teilen deiner Welten! 🎮
