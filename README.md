# BRUTAL//TRACK · RAW E2EE LIVE GPS TELEMETRY

> **An unapologetic, zero-leak real-time GPS tracking system** featuring a Native Android app (Kotlin + Jetpack Compose) and an End-to-End Encrypted (AES-256-GCM) Web Dashboard ready for free GitHub Pages hosting.

Designed in high-contrast **Neo-Brutalism**: 0px border-radius, 4px solid black borders, hard offset shadows, plain text, and instant tactile state changes.

---

## ⚡ How It Works (Zero-Setup & E2EE)

```
 [ Android Phone (GPS) ]
          │
          │ 1. Reads GPS (Lat, Lon, Alt, Speed, Bearing, Battery)
          │ 2. Derives 256-bit AES key via PBKDF2-SHA256 from your passphrase
          │ 3. Encrypts payload with AES-256-GCM (random 16B salt + 12B IV)
          ▼
 [ Free Public MQTT Relay (HiveMQ/EMQX) ]  <── Only sees Base64 encrypted ciphertext!
          │
          │ Relays encrypted packets over WebSockets (WSS)
          ▼
 [ PC / Web Dashboard (GitHub Pages) ]
          │
          │ 1. Prompts for your Secret Passphrase in the browser
          │ 2. Decrypts real-time with Web Crypto API (SubtleCrypto)
          │ 3. Renders tactical Leaflet map, speed gauge, and breadcrumb trails
```

- **100% Free Hosting**: The web dashboard is purely static HTML/CSS/JS and runs on **GitHub Pages**.
- **No Third-Party Accounts / No Database Required**: Uses public secure MQTT brokers (`broker.hivemq.com` or `broker.emqx.io`) over WebSockets.
- **Client-Side Encryption (E2EE)**: The broker and relay never see coordinates, speed, or device IDs. Decryption happens purely inside your PC's browser memory.

---

## 📂 Repository Layout

```
brutalloc-tracker/
├── android/                             # Native Android App (Kotlin + Jetpack Compose)
│   ├── app/src/main/
│   │   ├── java/com/brutal/tracker/
│   │   │   ├── crypto/CryptoManager.kt  # AES-256-GCM + PBKDF2 encryption engine
│   │   │   ├── data/Models.kt           # Data classes and telemetry contracts
│   │   │   ├── network/MqttPublisher.kt # Real-time Paho MQTT publisher
│   │   │   ├── service/LocationService.kt # Android Foreground Service (background tracking)
│   │   │   └── ui/                      # Jetpack Compose Neo-Brutalist UI & Theme
│   │   └── AndroidManifest.xml          # Background location & foreground service permissions
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
├── web/                                 # Static Web Dashboard for PC / Other Devices
│   ├── index.html                       # Tactical HUD, Leaflet map, packet inspector
│   ├── style.css                        # Strict Brutalism styles (4px borders, hard shadows)
│   ├── crypto.js                        # Web Crypto API AES-256-GCM decryption engine
│   └── app.js                           # MQTT.js WebSocket subscriber & Leaflet map engine
│
├── simulator/                           # Local PC Simulator for testing without phone
│   ├── simulate.js                      # Generates and broadcasts live encrypted test route
│   └── package.json
│
└── .github/workflows/
    └── deploy.yml                       # Automatic GitHub Pages deployment workflow
```

---

## 📱 1. Android App Setup

### Requirements
- Android 8.0+ (API 26+)
- Android Studio Ladybug / Koala / Iguana or later

### Building & Running
1. Open the [`android/`](android/) directory in **Android Studio**.
2. Let Gradle sync dependencies automatically.
3. Connect your Android device (or launch an emulator) with Developer Options & USB Debugging enabled.
4. Click **Run** (`Shift + F10`) or execute:
   ```bash
   ./gradlew assembleDebug
   ```
5. On the phone:
   - Enter your **Channel ID** (e.g. `my-rover-01`).
   - Enter your **Secret Passphrase** (e.g. `super-secret-key-99`).
   - Grant Location permissions.
   - Tap **`⚡ START BROADCAST`**.
   - A persistent foreground notification appears (`BRUTAL//TRACK: ACTIVE`), ensuring Android will not kill the tracking service even when the screen is locked.

---

## 💻 2. PC Web Dashboard (GitHub Pages)

### Instant Local Testing
You can open the web dashboard directly on your PC:
```bash
cd web
python3 -m http.server 8080
```
Open [`http://localhost:8080`](http://localhost:8080) in any browser (Chrome, Firefox, Zen, Brave, Edge, Safari).

### Free GitHub Pages Deployment
1. Push this repository to your GitHub account (e.g. `github.com/opmanus/brutalloc-tracker`).
2. Go to your repo **Settings** -> **Pages** -> **Source** -> Select **GitHub Actions**.
3. The included workflow [`.github/workflows/deploy.yml`](.github/workflows/deploy.yml) will automatically build and publish your dashboard to:
   ```
   https://opmanus.github.io/brutalloc-tracker/
   ```
4. Enter your **Channel ID** and **Passphrase**, then click **`CONNECT & LISTEN`**.
5. Once your Android app broadcasts, the map immediately pans to your location, draws live breadcrumbs, animates the velocity meter, and decodes packet logs!

### One-Click Sync Link
You can open or bookmark a pre-configured link with auto-connect:
```
https://opmanus.github.io/brutalloc-tracker/?channel=my-rover-01&pass=super-secret-key-99&autoconnect=1
```

---

## 🧪 3. Local PC Simulator (Test Without Phone)

To test the Web Dashboard on your PC right now without using a phone:
```bash
cd simulator
node simulate.js --channel alpha-squad-01 --pass hackme-if-you-can
```
Then open `web/index.html` in your browser. You will immediately see the simulated vehicle driving through the streets, updating live coordinates, speed, battery, and cryptographic envelopes!

---

## 🔒 Cryptographic Specifications

| Parameter | Specification |
|---|---|
| **Cipher** | `AES-256-GCM` (`AES/GCM/NoPadding`) |
| **Key Derivation** | `PBKDF2WithHmacSHA256` |
| **Iterations** | `65,536` |
| **Salt** | Cryptographically secure random 16 bytes (per packet) |
| **IV / Nonce** | Cryptographically secure random 12 bytes (per packet) |
| **Authentication Tag** | 128-bit GCM Auth Tag (prevents tampering) |
| **Relay Protocol** | MQTT 3.1.1 over WebSockets (WSS TLS Port 8884) & TCP (Port 1883) |

---

## 🎨 Design System: Brutalism
- **Colors**:
  - Yellow: `#FFFF00` (Cards, highlights)
  - Red: `#FF0000` (Speedometer, disconnects, alerts)
  - Green: `#00FF00` (Live indicators, decrypted pings)
  - Blue: `#0000FF` (Focus rings, breadcrumbs)
  - Magenta: `#FF00FF` (Altitude, crypto badges)
  - Black: `#000000` & White: `#FFFFFF`
- **Corners**: Flat 0px (`RoundedCornerShape(0.dp)`).
- **Borders & Shadows**: 3px - 4px solid black border, 4px - 8px hard offset black drop shadow.
- **Button Press Interaction**: Instant drop onto shadow (`transform: translate(4px, 4px); box-shadow: none;`).
- Verified against **`avoid-ai-design`** audit scanner with 0 findings.
