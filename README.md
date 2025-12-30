# Community App

Die **Community App** ist eine plattformübergreifende mobile Anwendung (Android & iOS), entwickelt mit **Kotlin Multiplatform (KMP)** und **Compose Multiplatform**. Sie dient als Schnittstelle zwischen Bürgern und Behörden und ermöglicht das Melden von Anliegen (Tickets), das Buchen von Terminen sowie den Abruf von behördlichen Mitteilungen.

Das Projekt beinhaltet zudem ein eigenes Backend, implementiert mit **Ktor**, das die REST-API und Datenbank bereitstellt.

Dieses Projekt entstand im Rahmen des Master-Moduls "Mobile Computing" im Studiengang Verwaltungsinformatik.

---

## 🛠 Tech Stack & Architektur

Das Projekt folgt den Prinzipien der **Clean Architecture**.

* **UI:** Jetpack Compose (Android) / Compose Multiplatform (iOS)
* **Architektur:** MVVM + Actions (MVI-artig)
* **Sprache:** Kotlin (Common, Android, iOS via Native Interop)
* **Netzwerk:** Ktor Client
* **Datenbank:** Room (SQLite)
* **Dependency Injection:** Koin
* **Bilder:** FileStorage & Coil (Android)

---

## 🚀 Voraussetzungen

* **Java Development Kit (JDK):** Version 17 oder höher.
* **Android Studio:** Aktuelle Version (mit KMM Plugin).
* **Xcode:** (Für iOS-Entwicklung) Aktuelle Version.
* **CocoaPods:** Für das Dependency Management unter iOS.

---

## ⚙️ Konfiguration

Bevor die App gestartet werden kann, müssen zwei Konfigurationsdateien angepasst werden.

### 1. API Keys (`local.properties`)

Die App nutzt **Geoapify** für die Adresssuche. Erstellen Sie (falls nicht vorhanden) eine Datei `local.properties` im Root-Verzeichnis des Projekts und fügen Sie Ihren Key hinzu:

    # community_app/local.properties
    sdk.dir=your_Android/sdk

    GEOAPIFY_KEY=your_geoapify_key
    GEOAPIFY_URL=https://api.geoapify.com/v1/geocode

### 2. Server-URL (`Constants.kt`)

Damit der Client den lokalen Server findet, muss die `BASE_URL` angepasst werden.
Datei: `shared/src/commonMain/kotlin/com/example/community_app/util/Constants.kt`

    object Constants {
        // Für Android Emulator:
        const val BASE_URL = "http://10.0.2.2:8080"
        
        // Für iOS Simulator oder physische Geräte im selben Netzwerk:
        // const val BASE_URL = "http://127.0.0.1:8080" // oder lokale IP z.B. 192.168.178.xx
        
        // ...
    }

---

## ▶️ Ausführung

### 1. Backend starten (Server)

Der Server muss laufen, bevor die App genutzt werden kann. Er generiert beim Start automatisch Testdaten (Nutzer, Ämter, Tickets) durch den `DatabaseSeeder`.

Führen Sie im Terminal im Root-Verzeichnis folgenden Befehl aus:

    ./gradlew :server:run

*Alternativ: Starten Sie die Run-Configuration "server" direkt in Android Studio.*

### 2. Android App starten

1.  Öffnen Sie das Projekt in Android Studio.
2.  Wählen Sie das Modul `composeApp` aus der Run-Configuration.
3.  Starten Sie die App auf einem Emulator.

**⚠️ WICHTIG: Standort im Emulator**
Die App zeigt Daten basierend auf dem aktuellen Standort an. Der Android-Emulator nutzt standardmäßig "Mountain View". Da die Testdaten für die Region **Hof** generiert werden, muss der Standort geändert werden:

1.  Öffnen Sie das Emulator-Menü (drei Punkte).
2.  Gehen Sie zu **Location**.
3.  Setzen Sie Koordinaten, z.B.: Latitude `50.325293`, Longitude `11.940531` (Alfons-Goppel-Platz 1).
4.  Klicken Sie auf "Set Location".

### 3. iOS App starten

1.  Stellen Sie sicher, dass `CocoaPods` installiert ist.
2.  Öffnen Sie das Verzeichnis `iosApp` und öffnen Sie den Workspace in Xcode (`iosApp.xcworkspace`).
3.  Starten Sie die App im Simulator.
4.  Simulieren Sie auch hier einen passenden Standort (via *Features > Location > Custom Location*).

---

## ✨ Features

* **Ticket-System:** Erstellen von Anliegen mit Bild-Upload und Standortwahl.
* **Terminbuchung:** Slots bei Behörden buchen und in den lokalen Kalender exportieren.
* **Offline-First:** Alle Daten werden lokal gecacht; Synchronisation erfolgt intelligent im Hintergrund.
* **Personalisierung:**
    * Dark/Light Mode unabhängig vom System.
    * Sprache (DE/EN) unabhängig vom System (inkl. App-Restart Logik).
* **Reaktivität:** Favoriten-Updates und Status-Änderungen werden live in der UI reflektiert.

---

## 📝 Lizenz

Dieses Projekt wurde zu Studienzwecken erstellt.