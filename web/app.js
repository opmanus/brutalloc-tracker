/**
 * BrutalTrack Web Application Logic
 * High-performance real-time telemetry decoding and mapping
 */

// Application State
const state = {
  client: null,
  connected: false,
  map: null,
  marker: null,
  accuracyCircle: null,
  polyline: null,
  trackHistory: [],
  pingsReceived: 0,
  audioEnabled: false,
  audioCtx: null,
  lastCoord: null
};

// UI Element References
const elements = {
  channelInput: document.getElementById("channelInput"),
  passphraseInput: document.getElementById("passphraseInput"),
  brokerSelect: document.getElementById("brokerSelect"),
  customBrokerGroup: document.getElementById("customBrokerGroup"),
  customBrokerInput: document.getElementById("customBrokerInput"),
  connectBtn: document.getElementById("connectBtn"),
  disconnectBtn: document.getElementById("disconnectBtn"),
  statusBadge: document.getElementById("statusBadge"),
  shareLinkBtn: document.getElementById("shareLinkBtn"),
  soundToggleBtn: document.getElementById("soundToggleBtn"),
  recenterBtn: document.getElementById("recenterBtn"),
  clearTrailBtn: document.getElementById("clearTrailBtn"),
  exportGpxBtn: document.getElementById("exportGpxBtn"),
  hudCoords: document.getElementById("hudCoords"),
  hudBearing: document.getElementById("hudBearing"),
  hudSpeed: document.getElementById("hudSpeed"),
  hudSpeedMph: document.getElementById("hudSpeedMph"),
  speedBar: document.getElementById("speedBar"),
  hudAltitude: document.getElementById("hudAltitude"),
  hudAccuracy: document.getElementById("hudAccuracy"),
  hudBattery: document.getElementById("hudBattery"),
  hudLastSeen: document.getElementById("hudLastSeen"),
  packetLog: document.getElementById("packetLog")
};

// Initialize Map
function initMap() {
  state.map = L.map("map", {
    zoomControl: true,
    attributionControl: true
  }).setView([20.0, 0.0], 3);

  // High-contrast clean OpenStreetMap tiles
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap</a>'
  }).addTo(state.map);

  // Brutalist Custom Marker Icon (Sharp rectangular crosshair)
  const brutalIcon = L.divIcon({
    className: "brutal-map-marker",
    html: `
      <div style="
        width: 24px;
        height: 24px;
        background: #FF0000;
        border: 3px solid #000000;
        box-shadow: 3px 3px 0 #000000;
        display: flex;
        align-items: center;
        justify-content: center;
      ">
        <div style="width: 6px; height: 6px; background: #FFFF00; border: 1px solid #000;"></div>
      </div>
    `,
    iconSize: [24, 24],
    iconAnchor: [12, 12]
  });

  state.marker = L.marker([0, 0], { icon: brutalIcon });
  state.polyline = L.polyline([], {
    color: "#0000FF",
    weight: 5,
    opacity: 0.9,
    lineJoin: "miter"
  }).addTo(state.map);
}

// Log to Cryptographic Raw Packet Stream Box
function logMessage(msg, isError = false) {
  const time = new Date().toTimeString().split(" ")[0];
  const color = isError ? "var(--brut-red)" : "var(--brut-green)";
  const line = `<div style="color: ${color}; margin-bottom: 4px;">[${time}] ${msg}</div>`;
  elements.packetLog.innerHTML = line + elements.packetLog.innerHTML;
}

// Brutalist 8-Bit Audio Chirp via Web Audio API
function playChirp() {
  if (!state.audioEnabled) return;
  try {
    if (!state.audioCtx) {
      state.audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    }
    const ctx = state.audioCtx;
    if (ctx.state === "suspended") ctx.resume();

    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = "square";
    osc.frequency.setValueAtTime(880, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(1760, ctx.currentTime + 0.08);

    gain.gain.setValueAtTime(0.15, ctx.currentTime);
    gain.gain.linearRampToValueAtTime(0.01, ctx.currentTime + 0.08);

    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.08);
  } catch (e) {
    console.error("Audio error", e);
  }
}

// Connect to MQTT Broker
function connectRelay() {
  const channel = elements.channelInput.value.trim();
  const passphrase = elements.passphraseInput.value.trim();

  if (!channel) {
    alert("Please enter a Tracking Channel ID");
    return;
  }
  if (!passphrase) {
    alert("Please enter your E2EE Secret Passphrase");
    return;
  }

  let brokerUrl = elements.brokerSelect.value;
  if (brokerUrl === "custom") {
    brokerUrl = elements.customBrokerInput.value.trim();
    if (!brokerUrl) {
      alert("Please enter a custom WebSocket URL (e.g. wss://...)");
      return;
    }
  }

  updateStatus("connecting", "CONNECTING // HANDSHAKE");
  logMessage(`Attempting WebSocket connection to: ${brokerUrl}`);

  const clientId = "brutal_pc_" + Math.random().toString(16).substring(2, 10);
  const topic = `brutalloc/${channel}`;

  try {
    state.client = mqtt.connect(brokerUrl, {
      clientId: clientId,
      clean: true,
      connectTimeout: 8000,
      reconnectPeriod: 5000
    });
  } catch (err) {
    updateStatus("disconnected", "CONNECTION ERROR");
    logMessage(`Failed to initiate MQTT client: ${err.message}`, true);
    return;
  }

  state.client.on("connect", () => {
    state.connected = true;
    updateStatus("live", `SUBSCRIBED // ${channel.toUpperCase()}`);
    logMessage(`CONNECTED TO BROKER. Subscribing to topic: "${topic}"`);
    elements.connectBtn.style.display = "none";
    elements.disconnectBtn.style.display = "inline-flex";

    state.client.subscribe(topic, { qos: 0 }, (err) => {
      if (err) {
        logMessage(`Subscription error: ${err.message}`, true);
      } else {
        logMessage(`LISTENING: Ready for encrypted location payloads on channel "${channel}"`);
      }
    });
  });

  state.client.on("message", async (recvTopic, payloadBuffer) => {
    state.pingsReceived++;
    const payloadStr = payloadBuffer.toString();
    
    try {
      const envelope = JSON.parse(payloadStr);
      logMessage(`RAW CIPHERTEXT RECEIVED (${payloadStr.length} bytes): IV=${envelope.iv?.substring(0, 8)}... CIPHER=${envelope.c?.substring(0, 16)}...`);

      // Decrypt using Web Crypto
      const decrypted = await BrutalCrypto.decryptPayload(envelope, passphrase);
      handleLocationUpdate(decrypted);
      playChirp();
    } catch (e) {
      logMessage(`PACKET ERROR: ${e.message}`, true);
    }
  });

  state.client.on("error", (err) => {
    logMessage(`MQTT Network Error: ${err.message}`, true);
    updateStatus("disconnected", "BROKER ERROR");
  });

  state.client.on("close", () => {
    if (state.connected) {
      logMessage("Connection closed.");
      updateStatus("disconnected", "DISCONNECTED");
      state.connected = false;
      elements.connectBtn.style.display = "inline-flex";
      elements.disconnectBtn.style.display = "none";
    }
  });
}

// Disconnect
function disconnectRelay() {
  if (state.client) {
    state.client.end(true);
    state.client = null;
  }
  state.connected = false;
  updateStatus("disconnected", "STANDBY // OFFLINE");
  elements.connectBtn.style.display = "inline-flex";
  elements.disconnectBtn.style.display = "none";
  logMessage("Terminated connection by user request.");
}

// Process and Display Decrypted Location
function handleLocationUpdate(loc) {
  const lat = parseFloat(loc.lat);
  const lon = parseFloat(loc.lon);
  if (isNaN(lat) || isNaN(lon)) return;

  const latLng = [lat, lon];
  state.lastCoord = latLng;
  state.trackHistory.push({
    lat,
    lon,
    alt: loc.alt || 0,
    speed: loc.spd || 0,
    time: loc.timestamp || Date.now()
  });

  // Update HUD
  elements.hudCoords.textContent = `${lat.toFixed(5)}, ${lon.toFixed(5)}`;
  
  const bearingVal = loc.bearing !== undefined ? Math.round(loc.bearing) : null;
  elements.hudBearing.textContent = bearingVal !== null ? `BEARING: ${bearingVal}°` : "BEARING: ---°";

  const speedKmh = loc.spd ? (loc.spd * 3.6).toFixed(1) : "0.0";
  const speedMph = loc.spd ? (loc.spd * 2.23694).toFixed(1) : "0.0";
  elements.hudSpeed.innerHTML = `${speedKmh} <span style="font-size: 1.2rem;">KM/H</span>`;
  elements.hudSpeedMph.textContent = `${speedMph} MPH`;

  // Update Speed Gauge Bar (max 160 km/h)
  const speedRatio = Math.min(100, (parseFloat(speedKmh) / 160) * 100);
  elements.speedBar.style.width = `${speedRatio}%`;

  elements.hudAltitude.textContent = loc.alt ? `${Math.round(loc.alt)} M` : "-- M";
  elements.hudAccuracy.textContent = loc.acc ? `ACCURACY RADIUS: ±${Math.round(loc.acc)} M` : "ACCURACY: ±-- M";

  const batteryText = loc.battery !== undefined ? `${loc.battery}%` : "--%";
  const chargingText = loc.isCharging ? " [CHARGING]" : "";
  elements.hudBattery.textContent = `${batteryText}${chargingText}`;

  const nowTime = new Date().toLocaleTimeString();
  elements.hudLastSeen.textContent = `PINGS: ${state.pingsReceived} | LAST: ${nowTime}`;

  // Update Leaflet Marker
  if (!state.map.hasLayer(state.marker)) {
    state.marker.addTo(state.map);
  }
  state.marker.setLatLng(latLng);

  // Update Accuracy Circle
  if (loc.acc) {
    if (!state.accuracyCircle) {
      state.accuracyCircle = L.circle(latLng, {
        radius: loc.acc,
        color: "#FF0000",
        fillColor: "#FFFF00",
        fillOpacity: 0.25,
        weight: 2
      }).addTo(state.map);
    } else {
      state.accuracyCircle.setLatLng(latLng);
      state.accuracyCircle.setRadius(loc.acc);
    }
  }

  // Update Polyline
  state.polyline.addLatLng(latLng);

  // Auto-center map if first point or user kept centered
  if (state.trackHistory.length === 1) {
    state.map.setView(latLng, 16);
  }

  // Popup content
  state.marker.bindPopup(`
    <div style="font-size: 0.9rem;">
      <div style="font-size: 1.1rem; margin-bottom: 4px;">TARGET TRACKED</div>
      <div>LAT: ${lat.toFixed(6)}</div>
      <div>LON: ${lon.toFixed(6)}</div>
      <div>SPD: ${speedKmh} KM/H</div>
      <div>BAT: ${batteryText}</div>
    </div>
  `);
}

function updateStatus(className, text) {
  elements.statusBadge.className = `status-pill ${className}`;
  elements.statusBadge.textContent = text;
}

// Recenter Map
function recenter() {
  if (state.lastCoord) {
    state.map.setView(state.lastCoord, 16);
  } else {
    alert("No location data received yet.");
  }
}

// Clear Trail
function clearTrail() {
  state.trackHistory = [];
  state.polyline.setLatLngs([]);
  if (state.accuracyCircle) {
    state.map.removeLayer(state.accuracyCircle);
    state.accuracyCircle = null;
  }
  logMessage("Cleared active breadcrumb trail.");
}

// Export Track as GPX File
function exportGPX() {
  if (state.trackHistory.length === 0) {
    alert("No coordinates recorded yet to export.");
    return;
  }

  let gpx = `<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="BrutalTrack" xmlns="http://www.topografix.com/GPX/1/1">
  <trk>
    <name>BrutalTrack Session ${new Date().toISOString()}</name>
    <trkseg>
`;

  state.trackHistory.forEach((pt) => {
    const timeIso = new Date(pt.time).toISOString();
    gpx += `      <trkpt lat="${pt.lat}" lon="${pt.lon}">
        <ele>${pt.alt}</ele>
        <time>${timeIso}</time>
        <speed>${pt.speed}</speed>
      </trkpt>\n`;
  });

  gpx += `    </trkseg>
  </trk>
</gpx>`;

  const blob = new Blob([gpx], { type: "application/gpx+xml" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `brutal-track-${Date.now()}.gpx`;
  a.click();
  URL.revokeObjectURL(url);
}

// Generate Shareable URL with Parameters
function copyShareUrl() {
  const channel = encodeURIComponent(elements.channelInput.value.trim());
  const pass = encodeURIComponent(elements.passphraseInput.value.trim());
  const broker = encodeURIComponent(elements.brokerSelect.value);

  const url = `${window.location.origin}${window.location.pathname}?channel=${channel}&pass=${pass}&broker=${broker}&autoconnect=1`;
  
  navigator.clipboard.writeText(url).then(() => {
    alert("COPIED TO CLIPBOARD:\n" + url);
  }).catch(() => {
    prompt("Copy this URL to view on PC or other devices:", url);
  });
}

// Load from URL query parameters
function parseQueryParams() {
  const params = new URLSearchParams(window.location.search);
  if (params.has("channel")) {
    elements.channelInput.value = params.get("channel");
  }
  if (params.has("pass")) {
    elements.passphraseInput.value = params.get("pass");
  }
  if (params.has("broker")) {
    const b = params.get("broker");
    if ([...elements.brokerSelect.options].some(o => o.value === b)) {
      elements.brokerSelect.value = b;
    } else {
      elements.brokerSelect.value = "custom";
      elements.customBrokerGroup.style.display = "block";
      elements.customBrokerInput.value = b;
    }
  }
  if (params.get("autoconnect") === "1") {
    setTimeout(connectRelay, 500);
  }
}

// Event Listeners
function setupEvents() {
  elements.connectBtn.addEventListener("click", connectRelay);
  elements.disconnectBtn.addEventListener("click", disconnectRelay);
  elements.recenterBtn.addEventListener("click", recenter);
  elements.clearTrailBtn.addEventListener("click", clearTrail);
  elements.exportGpxBtn.addEventListener("click", exportGPX);
  elements.shareLinkBtn.addEventListener("click", copyShareUrl);

  elements.brokerSelect.addEventListener("change", (e) => {
    elements.customBrokerGroup.style.display = e.target.value === "custom" ? "block" : "none";
  });

  elements.soundToggleBtn.addEventListener("click", () => {
    state.audioEnabled = !state.audioEnabled;
    elements.soundToggleBtn.textContent = `AUDIO CHIRP: ${state.audioEnabled ? "ON" : "OFF"}`;
    if (state.audioEnabled) playChirp();
  });
}

// Startup
window.addEventListener("DOMContentLoaded", () => {
  initMap();
  setupEvents();
  parseQueryParams();
});
