# ARKA//TRACK · PRIVATE MULTI-DEVICE E2EE GPS TELEMETRY

> **Zero-leak real-time GPS tracking system** featuring a Native Android app (Kotlin + Jetpack Compose) professionally signed by **Arka Mandal**, and an End-to-End Encrypted (AES-256-GCM) Web Dashboard hosted on **GitHub Pages**.

---

## ⚡ Key Highlights

- **Professionally Signed**: Signed with a private 2048-bit RSA release key (`CN=Arka Mandal, O=Arka Mandal, C=IN`). Keystore credentials are encrypted via GitHub Secrets and never exposed in the repo.
- **Zero Manual Channel Setup**: No copying channel IDs or manual tokens. The channel is deterministically auto-derived from your Master Passcode via SHA-256 (`arka-mandal-<passcode>`).
- **Private Access Gatekeeper**: The web dashboard is secured with an access screen — only you can unlock the terminal with your Master Passcode.
- **Multi-Device Support & Device Names**: Track multiple devices simultaneously (e.g. *"Arka's Main Phone"*, *"Car Tracker"*, *"Work Device"*) on a single tactical radar map with live battery & velocity tabs.
- **Refined Subtle Design**: Sleek Dark Mode (default) with CartoDB Dark Matter tiles, clean Light Mode, and subtle telemetry tints (cyan, rose, emerald, purple) with 0 eye-strain.

---

## 🔒 Security Architecture

```
 [ Android Devices ] (e.g. "Arka's Phone", "Car GPS")
         │
         │ 1. Device Name + Master Passcode entered once
         │ 2. Channel auto-derived via SHA-256
         │ 3. Payload encrypted with AES-256-GCM (16B salt + 12B IV)
         ▼
 [ Secure Public WSS Relay (HiveMQ / EMQX) ]  <── Relays encrypted ciphertext only
         │
         ▼
 [ Private Web Dashboard (GitHub Pages) ]
         │
         │ 1. Unlocked with your Master Passcode
         │ 2. Decrypts all multi-device streams on-the-fly in browser memory
         │ 3. Maps individual device pins, speedometers, & breadcrumbs
```

---

## 📱 Android App (Signed by Arka Mandal)

### Download Ready-to-Install APK
Download the signed APK directly from the repository Releases:
`https://github.com/opmanus/brutalloc-tracker/releases`

### Features
1. **Background Foreground Service**: Continues tracking and broadcasting when the phone is locked or screen is off.
2. **Device Naming**: Customize your device name directly in the app.
3. **Hardware Battery Monitoring**: Broadcasts battery percentage and charging status in real time.

---

## 🌐 Private Web Dashboard

Open: **`https://opmanus.github.io/brutalloc-tracker/`**

1. Enter your Master Passcode to unlock the terminal.
2. The dashboard will automatically connect and listen to all your devices broadcasting under that passcode.
3. Switch between **All Devices (Fleet View)** or focus on any individual device tab.
4. Toggle **Dark / Light Mode** anytime with the header button.
