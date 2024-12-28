# Community Crackables Plugin

IN ENTWICKLUNG - IN DEVELOPMENT - IN ENTWICKLUNG - IN DEVELOPMENT
Ein Plugin für den Arcturus Habbo Emulator, das Community-basierte Crackables implementiert. User können gemeinsam an einem Crackable arbeiten und erhalten Belohnungen basierend auf ihrer individuellen Beteiligung.

## Features

### Crackable System
- Community-basiertes Cracken von Möbeln
- Zeitlimit mit automatischer Entfernung
- Echtzeit-Fortschrittsanzeige
- Versteckte Belohnungsstufen

### Belohnungstypen
- Taler (Credits)
- Duckets
- Diamanten
- Badges
- Möbel (Einzeln oder mehrfach)

### Technische Features
- Persistente Speicherung in MySQL
- Automatische Belohnungsverteilung
- Offline-Belohnungssystem
- Fortschritts-Tracking pro User
- Automatische Datenbankbereinigung

## Installation

1. Voraussetzungen:
```
- Java 11 oder höher
- Maven
- MySQL Datenbank
- Arcturus Emulator
```

2. Repository klonen:
```bash
git clone https://github.com/DEIN-USERNAME/community-crackables.git
cd community-crackables
```

3. Mit Maven kompilieren:
```bash
mvn clean package
```

4. Plugin installieren:
- Die generierte .jar aus dem `target` Ordner in den `plugins` Ordner des Emulators kopieren
- Emulator neustarten

## Verwendung

### Command-Syntax
```
:createcrackable <furniture_id> <sprite_id> <totalHits> <timeInMinutes> <reward1MinHits>:<type>:<id>:<amount> ...
```

### Parameter
- `furniture_id`: ID des Möbelstücks
- `sprite_id`: Sprite-ID für die Anzeige
- `totalHits`: Benötigte Gesamtanzahl an Klicks
- `timeInMinutes`: Zeitlimit in Minuten
- `rewardX`: Belohnungsstufen im Format `minHits:type:id:amount`

### Belohnungstypen
```
credits:0:amount     - Taler (ID wird ignoriert)
duckets:0:amount    - Duckets (ID wird ignoriert)
diamonds:0:amount   - Diamanten (ID wird ignoriert)
badge:code:1        - Badge mit Code
furniture:id:amount - Möbel mit ID und Anzahl
```

### Beispiele

1. Einfaches Crackable mit Taler:
```
:createcrackable 1234 5678 1000 60 1:credits:0:100
```

2. Komplexes Crackable mit verschiedenen Belohnungen:
```
:createcrackable 1234 5678 100000 120 1:credits:0:100 100:furniture:9876:1 1000:badge:CCR2:1 5000:diamonds:0:20
```

Dies erstellt:
- Ein Crackable mit Möbel-ID 1234
- 100.000 benötigte Hits
- 120 Minuten Zeitlimit
- Belohnungsstufen:
  - Ab 1 Hit: 100 Taler
  - Ab 100 Hits: 1x Möbelstück #9876 
  - Ab 1000 Hits: Badge CCR2
  - Ab 5000 Hits: 20 Diamanten

## Permissions

Das Plugin benötigt folgende Berechtigungen:
- `cmd_createcrackable`: Für das Erstellen von Community Crackables

## Datenbank

Das Plugin erstellt automatisch folgende Tabellen:
```sql
community_crackables             - Haupttabelle für Crackables
community_crackables_rewards     - Belohnungsstufen
community_crackables_participants - Teilnehmer und ihre Hits
community_crackables_offline     - Belohnungen für Offline-User
```

## Entwicklung

### Projektstruktur
```
community-crackables/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── palokos/
        │           └── communitycrackable/
        │               ├── CommunityCrackablePlugin.java
        │               ├── CommunityCrackable.java
        │               ├── CreateCommunityCrackableCommand.java
        │               ├── models/
        │               │   ├── CrackableTimer.java
        │               │   └── RewardTier.java
        │               └── utils/
        │                   └── DatabaseManager.java
        └── resources/
            └── plugin.json
```

### Build
```bash
mvn clean package
```

## License

[MIT License](LICENSE)

## Author

- Name: Palokos
- GitHub: [PalokosDev](https://github.com/PalokosDev)

## Support

Bei Fragen oder Problemen:
1. Issue auf GitHub erstellen
2. Discord: Palokos
