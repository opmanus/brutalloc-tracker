// avoid-ai-design-ignore-file: SD8
/**
 * ARKA MANDAL // Private GPS Telemetry Hub
 * Multi-device auto-discovery, zero manual keys, E2EE AES-256-GCM
 */

// Application State
const state = {
  masterPass: "",
  derivedChannel: "",
  client: null,
  connected: false,
  map: null,
  tileLayer: null,
  currentTheme: "dark",
  currentMapType: "osm", // "osm" or "satellite"
  audioEnabled: false,
  audioCtx: null,
  activeDeviceId: "all", // "all" or specific deviceId
  devices: {} // deviceId -> { id, name, lat, lon, alt, spd, acc, bearing, battery, isCharging, timestamp, marker, polyline, history: [] }
};

// UI Elements Cache
const elements = {
  gatekeeperModal: document.getElementById("gatekeeperModal"),
  mainDashboard: document.getElementById("mainDashboard"),
  masterPassInput: document.getElementById("masterPassInput"),
  rememberPassCheck: document.getElementById("rememberPassCheck"),
  themeToggleBtn: document.getElementById("themeToggleBtn"),
  lockTerminalBtn: document.getElementById("lockTerminalBtn"),
  statusBadge: document.getElementById("statusBadge"),
  deviceCount: document.getElementById("deviceCount"),
  deviceListTabs: document.getElementById("deviceListTabs"),
  hudCoords: document.getElementById("hudCoords"),
  hudBearing: document.getElementById("hudBearing"),
  hudSpeed: document.getElementById("hudSpeed"),
  hudSpeedMph: document.getElementById("hudSpeedMph"),
  speedBar: document.getElementById("speedBar"),
  hudAltitude: document.getElementById("hudAltitude"),
  hudAccuracy: document.getElementById("hudAccuracy"),
  hudBattery: document.getElementById("hudBattery"),
  hudLastSeen: document.getElementById("hudLastSeen"),
  hudDeviceNameLabel: document.getElementById("hudDeviceNameLabel"),
  mapActiveDeviceTitle: document.getElementById("mapActiveDeviceTitle"),
  layerToggleBtn: document.getElementById("layerToggleBtn"),
  brokerSelect: document.getElementById("brokerSelect"),
  customBrokerGroup: document.getElementById("customBrokerGroup"),
  customBrokerInput: document.getElementById("customBrokerInput"),
  recenterBtn: document.getElementById("recenterBtn"),
  clearTrailBtn: document.getElementById("clearTrailBtn"),
  exportGpxBtn: document.getElementById("exportGpxBtn"),
  copyShareBtn: document.getElementById("copyShareBtn"),
  soundToggleBtn: document.getElementById("soundToggleBtn"),
  packetLog: document.getElementById("packetLog")
};

// Map Tile Providers (100% Free, Zero API Keys, Superior India & Global Coverage)
const TILE_PROVIDERS = {
  osm: {
    label: "MAP: STREET (OSM)",
    url: "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
    options: {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> contributors'
    }
  },
  satellite: {
    label: "MAP: SATELLITE (ESRI)",
    url: "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}",
    options: {
      maxZoom: 19,
      attribution: 'Tiles &copy; <a href="https://www.esri.com/" target="_blank">Esri</a> &mdash; Source: Esri, Maxar, Earthstar Geographics'
    }
  }
};

// Device Accent Color Palette
const DEVICE_COLORS = [
  "#38bdf8", // Cyan
  "#34d399", // Emerald
  "#fb7185", // Rose
  "#c084fc", // Purple
  "#fbbf24", // Amber
  "#60a5fa"  // Blue
];

// Helper: Derive Channel from Passphrase via SHA-256
async function deriveChannelFromPass(passphrase) {
  const enc = new TextEncoder();
  const data = enc.encode("arka-mandal-" + passphrase.trim());
  const hashBuf = await window.crypto.subtle.digest("SHA-256", data);
  const hashArray = Array.from(new Uint8Array(hashBuf));
  const hex = hashArray.map(b => b.toString(16).padStart(2, "0")).join("");
  return hex.substring(0, 16);
}

// Gatekeeper: Unlock Dashboard
async function unlockDashboard() {
  const pass = elements.masterPassInput.value.trim();
  if (!pass) {
    alert("Please enter your Master Passcode.");
    return;
  }

  state.masterPass = pass;
  state.derivedChannel = await deriveChannelFromPass(pass);

  if (elements.rememberPassCheck.checked) {
    localStorage.setItem("arka_master_pass", pass);
  } else {
    localStorage.removeItem("arka_master_pass");
  }

  elements.gatekeeperModal.style.display = "none";
  elements.mainDashboard.style.display = "block";

  // Initialize Map if not ready
  if (!state.map) {
    initMap();
  }

  connectRelay();
}

// Lock Terminal
function lockTerminal() {
  disconnectRelay();
  localStorage.removeItem("arka_master_pass");
  elements.mainDashboard.style.display = "none";
  elements.gatekeeperModal.style.display = "flex";
  elements.masterPassInput.value = "";
}

// Update Map Tile Layer with Free Providers & Tactical Theme Filters
function updateMapTiles() {
  if (!state.map) return;
  if (state.tileLayer) {
    state.map.removeLayer(state.tileLayer);
  }

  const provider = TILE_PROVIDERS[state.currentMapType] || TILE_PROVIDERS.osm;
  state.tileLayer = L.tileLayer(provider.url, provider.options).addTo(state.map);

  // Apply or remove dark mode inversion on tile pane
  const tilePane = state.map.getPanes().tilePane;
  if (tilePane) {
    if (state.currentMapType === "osm" && state.currentTheme === "dark") {
      tilePane.classList.add("tactical-dark");
    } else {
      tilePane.classList.remove("tactical-dark");
    }
  }

  if (elements.layerToggleBtn) {
    elements.layerToggleBtn.textContent = provider.label;
  }
}

// Toggle between Free OpenStreetMap and High-Res Esri Satellite
function toggleMapLayer() {
  state.currentMapType = state.currentMapType === "osm" ? "satellite" : "osm";
  updateMapTiles();
}

// Theme Switching
function setTheme(theme) {
  state.currentTheme = theme;
  document.documentElement.setAttribute("data-theme", theme);
  localStorage.setItem("arka_theme", theme);

  if (elements.themeToggleBtn) {
    elements.themeToggleBtn.textContent = theme === "dark" ? "THEME: DARK ☾" : "THEME: LIGHT ☼";
  }

  updateMapTiles();
}

function initTheme() {
  const saved = localStorage.getItem("arka_theme");
  if (saved && (saved === "dark" || saved === "light")) {
    setTheme(saved);
  } else {
    setTheme("dark"); // Default to Dark
  }
}

// Initialize Leaflet Map (Centered over India by default)
function initMap() {
  state.map = L.map("map", {
    zoomControl: true,
    attributionControl: true
  }).setView([20.5937, 78.9629], 5);

  updateMapTiles();
}

// Log message to raw packet drawer
function logMessage(msg, isError = false) {
  const time = new Date().toTimeString().split(" ")[0];
  const color = isError ? "var(--accent-red)" : "var(--log-text)";
  const line = `<div style="color: ${color}; margin-bottom: 3px;">[${time}] ${msg}</div>`;
  elements.packetLog.innerHTML = line + elements.packetLog.innerHTML;
}

// 8-bit Audio Chirp
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
    osc.type = "sine";
    osc.frequency.setValueAtTime(920, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(1440, ctx.currentTime + 0.06);

    gain.gain.setValueAtTime(0.1, ctx.currentTime);
    gain.gain.linearRampToValueAtTime(0.01, ctx.currentTime + 0.06);

    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.06);
  } catch (e) {
    console.error("Audio error", e);
  }
}

// Connect to MQTT Broker
function connectRelay() {
  if (!state.derivedChannel) return;

  let brokerUrl = elements.brokerSelect.value;
  if (brokerUrl === "custom") {
    brokerUrl = elements.customBrokerInput.value.trim();
  }

  updateStatus("connecting", "CONNECTING");
  logMessage(`Connecting to WSS Broker for Channel [${state.derivedChannel}]...`);

  const clientId = "arka_dash_" + Math.random().toString(16).substring(2, 10);
  const baseTopic = `brutalloc/${state.derivedChannel}`;
  const wildcardTopic = `brutalloc/${state.derivedChannel}/#`;

  try {
    state.client = mqtt.connect(brokerUrl, {
      clientId: clientId,
      clean: true,
      connectTimeout: 8000,
      reconnectPeriod: 5000
    });
  } catch (err) {
    updateStatus("disconnected", "ERROR");
    logMessage(`MQTT connect failed: ${err.message}`, true);
    return;
  }

  state.client.on("connect", () => {
    state.connected = true;
    updateStatus("live", "STREAM ACTIVE");
    logMessage(`BROKER CONNECTED. Listening to private stream.`);

    state.client.subscribe([baseTopic, wildcardTopic], { qos: 1 }, (err) => {
      if (err) {
        logMessage(`Subscription error: ${err.message}`, true);
      } else {
        logMessage(`READY: Subscribed to [${baseTopic}] and [${wildcardTopic}]`);
      }
    });
  });

  state.client.on("message", async (recvTopic, payloadBuffer) => {
    const payloadStr = payloadBuffer.toString();
    try {
      const envelope = JSON.parse(payloadStr);
      // Decrypt using Web Crypto
      const decrypted = await BrutalCrypto.decryptPayload(envelope, state.masterPass);
      handleLocationUpdate(decrypted);
      playChirp();
      logMessage(`DECRYPTED PACKET from "${decrypted.deviceName || decrypted.deviceId}" (Speed: ${decrypted.spd || 0} m/s)`);
    } catch (e) {
      logMessage(`DECRYPTION ERROR: ${e.message}`, true);
    }
  });

  state.client.on("error", (err) => {
    updateStatus("disconnected", "BROKER ERROR");
  });

  state.client.on("close", () => {
    if (state.connected) {
      updateStatus("disconnected", "DISCONNECTED");
      state.connected = false;
    }
  });
}

function disconnectRelay() {
  if (state.client) {
    state.client.end(true);
    state.client = null;
  }
  state.connected = false;
  updateStatus("disconnected", "OFFLINE");
}

// Process Incoming Decrypted Location Packet
function handleLocationUpdate(loc) {
  const deviceId = loc.deviceId || "DEV-DEFAULT";
  const deviceName = loc.deviceName || deviceId;
  const lat = parseFloat(loc.lat);
  const lon = parseFloat(loc.lon);
  if (isNaN(lat) || isNaN(lon)) return;

  const latLng = [lat, lon];
  const colorIndex = Object.keys(state.devices).length % DEVICE_COLORS.length;
  const devColor = DEVICE_COLORS[colorIndex];

  let dev = state.devices[deviceId];
  const isNewDevice = !dev;

  if (isNewDevice) {
    // Create new device tracking object
    dev = {
      id: deviceId,
      name: deviceName,
      color: devColor,
      lat: lat,
      lon: lon,
      alt: loc.alt || 0,
      spd: loc.spd || 0,
      acc: loc.acc || 0,
      bearing: loc.bearing || 0,
      battery: loc.battery !== undefined ? loc.battery : 100,
      isCharging: !!loc.isCharging,
      timestamp: loc.timestamp || Date.now(),
      history: [],
      marker: null,
      polyline: null
    };

    // Create marker
    const markerHtml = `
      <div style="
        width: 26px; height: 26px;
        background: ${devColor};
        border: 2px solid #ffffff;
        box-shadow: 0 0 10px ${devColor};
        display: flex; align-items: center; justify-content: center;
        font-size: 11px; font-weight: 900; color: #000;
      ">
        ▲
      </div>
    `;
    const icon = L.divIcon({
      className: "device-marker",
      html: markerHtml,
      iconSize: [26, 26],
      iconAnchor: [13, 13]
    });

    dev.marker = L.marker(latLng, { icon: icon }).addTo(state.map);
    dev.polyline = L.polyline([], {
      color: devColor,
      weight: 4,
      opacity: 0.85
    }).addTo(state.map);

    state.devices[deviceId] = dev;
    renderDeviceTabs();
  } else {
    // Update existing device
    dev.name = deviceName;
    dev.lat = lat;
    dev.lon = lon;
    dev.alt = loc.alt || dev.alt;
    dev.spd = loc.spd !== undefined ? loc.spd : dev.spd;
    dev.acc = loc.acc || dev.acc;
    dev.bearing = loc.bearing || dev.bearing;
    dev.battery = loc.battery !== undefined ? loc.battery : dev.battery;
    dev.isCharging = !!loc.isCharging;
    dev.timestamp = loc.timestamp || Date.now();

    dev.marker.setLatLng(latLng);
  }

  // Record history
  dev.history.push({ lat, lon, time: dev.timestamp });
  dev.polyline.addLatLng(latLng);

  // Update popup
  const speedKmh = (dev.spd * 3.6).toFixed(1);
  dev.marker.bindPopup(`
    <div style="font-size: 0.85rem; padding: 4px;">
      <div style="font-weight: 800; font-size: 1rem; color: ${dev.color}; margin-bottom: 2px;">
        ${dev.name}
      </div>
      <div>SPEED: ${speedKmh} KM/H</div>
      <div>BATTERY: ${dev.battery}% ${dev.isCharging ? '[CHARGING]' : ''}</div>
      <div>COORDS: ${lat.toFixed(5)}, ${lon.toFixed(5)}</div>
    </div>
  `);

  // If this device is active (or fleet view), update HUD
  if (state.activeDeviceId === "all" || state.activeDeviceId === deviceId) {
    updateHUD(dev);
  }

  // If first location received, center camera
  if (Object.keys(state.devices).length === 1 && dev.history.length === 1) {
    state.map.setView(latLng, 16);
  }
}

// Update HUD Display
function updateHUD(dev) {
  if (!dev) return;

  elements.hudCoords.textContent = `${dev.lat.toFixed(5)}, ${dev.lon.toFixed(5)}`;
  elements.hudBearing.textContent = `BEARING: ${Math.round(dev.bearing)}° N`;

  const speedKmh = (dev.spd * 3.6).toFixed(1);
  const speedMph = (dev.spd * 2.23694).toFixed(1);
  elements.hudSpeed.innerHTML = `${speedKmh} <span style="font-size: 1.1rem; opacity: 0.7;">KM/H</span>`;
  elements.hudSpeedMph.textContent = `${speedMph} MPH`;

  const speedPct = Math.min(100, (parseFloat(speedKmh) / 160) * 100);
  elements.speedBar.style.width = `${speedPct}%`;

  elements.hudAltitude.textContent = `${Math.round(dev.alt)} M`;
  elements.hudAccuracy.textContent = `ACCURACY RADIUS: ±${Math.round(dev.acc)} M`;

  elements.hudDeviceNameLabel.textContent = dev.name.toUpperCase();
  elements.hudBattery.textContent = `${dev.battery}% ${dev.isCharging ? '[CHARGING]' : ''}`;
  elements.hudLastSeen.textContent = `PINGS: ${dev.history.length} | LAST: ${new Date(dev.timestamp).toLocaleTimeString()}`;
}

// Render Multi-Device Tab Bar
function renderDeviceTabs() {
  const container = elements.deviceListTabs;
  container.innerHTML = "";

  const totalDevs = Object.keys(state.devices).length;
  elements.deviceCount.textContent = `${totalDevs} ACTIVE`;

  // Fleet / All Devices Tab
  const allTab = document.createElement("div");
  allTab.className = `device-tab ${state.activeDeviceId === "all" ? "active" : ""}`;
  allTab.innerHTML = `<span class="dot live"></span><span>ALL DEVICES (${totalDevs})</span>`;
  allTab.onclick = () => selectDevice("all");
  container.appendChild(allTab);

  // Individual Device Tabs
  Object.values(state.devices).forEach(dev => {
    const tab = document.createElement("div");
    tab.className = `device-tab ${state.activeDeviceId === dev.id ? "active" : ""}`;
    tab.style.borderColor = state.activeDeviceId === dev.id ? dev.color : "";
    tab.innerHTML = `
      <span class="dot" style="background: ${dev.color}; box-shadow: 0 0 6px ${dev.color};"></span>
      <span>${dev.name} (${dev.battery}%)</span>
    `;
    tab.onclick = () => selectDevice(dev.id);
    container.appendChild(tab);
  });
}

// Select Active Device
function selectDevice(id) {
  state.activeDeviceId = id;
  renderDeviceTabs();

  if (id === "all") {
    elements.mapActiveDeviceTitle.textContent = "RADAR MAP · ALL DEVICES ACTIVE";
    // Fit bounds of all devices
    const bounds = L.latLngBounds([]);
    Object.values(state.devices).forEach(d => bounds.extend([d.lat, d.lon]));
    if (bounds.isValid()) state.map.fitBounds(bounds, { padding: [50, 50], maxZoom: 16 });
  } else {
    const dev = state.devices[id];
    if (dev) {
      elements.mapActiveDeviceTitle.textContent = `TRACKING: ${dev.name.toUpperCase()}`;
      updateHUD(dev);
      state.map.setView([dev.lat, dev.lon], 16);
      dev.marker.openPopup();
    }
  }
}

function updateStatus(className, text) {
  elements.statusBadge.className = `status-pill ${className}`;
  elements.statusBadge.textContent = text;
}

// Recenter Map
function recenter() {
  if (state.activeDeviceId === "all") {
    const bounds = L.latLngBounds([]);
    Object.values(state.devices).forEach(d => bounds.extend([d.lat, d.lon]));
    if (bounds.isValid()) state.map.fitBounds(bounds, { padding: [50, 50], maxZoom: 16 });
  } else {
    const dev = state.devices[state.activeDeviceId];
    if (dev) state.map.setView([dev.lat, dev.lon], 16);
  }
}

// Clear Active Trails
function clearTrails() {
  Object.values(state.devices).forEach(dev => {
    dev.history = [];
    dev.polyline.setLatLngs([]);
  });
  logMessage("Cleared breadcrumb trails for all devices.");
}

// Export Multi-Device GPX
function exportGPX() {
  const devs = Object.values(state.devices);
  if (devs.length === 0) {
    alert("No location data recorded yet.");
    return;
  }

  let gpx = `<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="ArkaTrack" xmlns="http://www.topografix.com/GPX/1/1">\n`;

  devs.forEach(dev => {
    gpx += `  <trk>\n    <name>${dev.name}</name>\n    <trkseg>\n`;
    dev.history.forEach(pt => {
      gpx += `      <trkpt lat="${pt.lat}" lon="${pt.lon}"><time>${new Date(pt.time).toISOString()}</time></trkpt>\n`;
    });
    gpx += `    </trkseg>\n  </trk>\n`;
  });
  gpx += `</gpx>`;

  const blob = new Blob([gpx], { type: "application/gpx+xml" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `arka-telemetry-${Date.now()}.gpx`;
  a.click();
  URL.revokeObjectURL(url);
}

// Copy One-Click Pairing Link
function copyPairingLink() {
  const url = `${window.location.origin}${window.location.pathname}?pass=${encodeURIComponent(state.masterPass)}&autoconnect=1`;
  navigator.clipboard.writeText(url).then(() => {
    alert("COPIED TO CLIPBOARD:\n" + url);
  }).catch(() => {
    prompt("Your private one-click sync link:", url);
  });
}

// Setup Event Listeners
function setupEvents() {
  elements.themeToggleBtn.addEventListener("click", () => {
    const next = state.currentTheme === "dark" ? "light" : "dark";
    setTheme(next);
  });

  elements.lockTerminalBtn.addEventListener("click", lockTerminal);
  if (elements.layerToggleBtn) {
    elements.layerToggleBtn.addEventListener("click", toggleMapLayer);
  }
  elements.recenterBtn.addEventListener("click", recenter);
  elements.clearTrailBtn.addEventListener("click", clearTrails);
  elements.exportGpxBtn.addEventListener("click", exportGPX);
  elements.copyShareBtn.addEventListener("click", copyPairingLink);

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
  initTheme();
  setupEvents();

  // Check URL params or localStorage for auto-unlock
  const params = new URLSearchParams(window.location.search);
  const urlPass = params.get("pass");
  const savedPass = localStorage.getItem("arka_master_pass");

  if (urlPass) {
    elements.masterPassInput.value = urlPass;
    unlockDashboard();
  } else if (savedPass) {
    elements.masterPassInput.value = savedPass;
    unlockDashboard();
  }
});
