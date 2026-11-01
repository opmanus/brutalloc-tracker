const mqtt = require("mqtt");
const crypto = require("crypto");

// Configuration from CLI or defaults
const args = process.argv.slice(2);
let channel = "alpha-squad-01";
let passphrase = "hackme-if-you-can";
let brokerUrl = "mqtt://broker.hivemq.com:1883";

for (let i = 0; i < args.length; i++) {
  if (args[i] === "--channel" && args[i + 1]) channel = args[++i];
  if (args[i] === "--pass" && args[i + 1]) passphrase = args[++i];
  if (args[i] === "--broker" && args[i + 1]) brokerUrl = args[++i];
}

console.log("=================================================");
console.log(" BRUTAL//TRACK · GPS SIMULATION TRANSMITTER");
console.log("=================================================");
console.log(`[CHANNEL]   : ${channel}`);
console.log(`[PASSPHRASE]: ${passphrase}`);
console.log(`[BROKER]    : ${brokerUrl}`);
console.log(`[TOPIC]     : brutalloc/${channel}`);
console.log("-------------------------------------------------");

function encryptPayload(dataObj, pass) {
  const salt = crypto.randomBytes(16);
  const iv = crypto.randomBytes(12);
  const key = crypto.pbkdf2Sync(pass, salt, 65536, 32, "sha256");

  const cipher = crypto.createCipheriv("aes-256-gcm", key, iv);
  const jsonStr = JSON.stringify(dataObj);
  let enc = cipher.update(jsonStr, "utf8");
  enc = Buffer.concat([enc, cipher.final()]);
  const tag = cipher.getAuthTag();
  const cipherWithTag = Buffer.concat([enc, tag]);

  return {
    s: salt.toString("base64"),
    iv: iv.toString("base64"),
    c: cipherWithTag.toString("base64")
  };
}

// Starting coordinates (San Francisco Embarcadero / Market St)
let lat = 37.7935;
let lon = -122.3958;
let battery = 94;
let speedKmh = 38.0;
let bearing = 225.0;
let altitude = 12.0;
let pingCount = 0;

const client = mqtt.connect(brokerUrl);

client.on("connect", () => {
  console.log(">> CONNECTED TO MQTT BROKER. Broadcasting encrypted packets every 2.5s...\n");

  setInterval(() => {
    pingCount++;
    
    // Move along bearing
    const distKm = (speedKmh / 3600.0) * 2.5; // distance moved in 2.5s
    const dLat = (distKm / 111.0) * Math.cos(bearing * (Math.PI / 180.0));
    const dLon = (distKm / (111.0 * Math.cos(lat * (Math.PI / 180.0)))) * Math.sin(bearing * (Math.PI / 180.0));

    lat += dLat;
    lon += dLon;

    // Slight variance in speed & bearing for realism
    speedKmh = Math.max(15, Math.min(65, speedKmh + (Math.random() * 6 - 3)));
    bearing = (bearing + (Math.random() * 10 - 5) + 360) % 360;
    if (pingCount % 15 === 0 && battery > 5) battery--;

    const locationPacket = {
      deviceId: "SIMULATOR_TEST_UNIT_01",
      lat: parseFloat(lat.toFixed(6)),
      lon: parseFloat(lon.toFixed(6)),
      alt: Math.round(altitude),
      acc: 3.5,
      spd: parseFloat((speedKmh / 3.6).toFixed(2)), // in m/s
      bearing: Math.round(bearing),
      battery: battery,
      isCharging: false,
      timestamp: Date.now()
    };

    const envelope = encryptPayload(locationPacket, passphrase);
    const envelopeJson = JSON.stringify(envelope);

    client.publish(`brutalloc/${channel}`, envelopeJson, { qos: 0 }, (err) => {
      if (err) {
        console.error(`[PING #${pingCount}] Send Error:`, err);
      } else {
        const timeStr = new Date().toLocaleTimeString();
        console.log(`[${timeStr}] PING #${pingCount} -> LAT: ${lat.toFixed(5)} LON: ${lon.toFixed(5)} | ${speedKmh.toFixed(1)} KM/H | BAT: ${battery}%`);
        console.log(`             ENCRYPTED: { IV: "${envelope.iv.substring(0, 10)}...", CIPHER: "${envelope.c.substring(0, 24)}..." } (${envelopeJson.length} bytes)`);
      }
    });
  }, 2500);
});

client.on("error", (err) => {
  console.error("MQTT Error:", err);
});
