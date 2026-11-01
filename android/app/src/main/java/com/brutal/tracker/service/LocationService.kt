package com.brutal.tracker.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.brutal.tracker.R
import com.brutal.tracker.crypto.CryptoManager
import com.brutal.tracker.data.LocationPacket
import com.brutal.tracker.data.ServiceState
import com.brutal.tracker.network.MqttPublisher
import com.brutal.tracker.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationService : Service() {

    private val tag = "LocationService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var mqttPublisher: MqttPublisher? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var deviceName: String = "Arka's Phone"
    private var channelId: String = "alpha-squad-01"
    private var passphrase: String = "hackme-if-you-can"
    private var brokerHost: String = "broker.hivemq.com"
    private var brokerPort: Int = 1883
    private var intervalSec: Int = 3
    private var deviceId: String = "DEV"

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val CHANNEL_ID_NOTIFICATION = "brutal_tracker_channel"
        const val NOTIFICATION_ID = 1337

        const val EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME"
        const val EXTRA_CHANNEL = "EXTRA_CHANNEL"
        const val EXTRA_PASSPHRASE = "EXTRA_PASSPHRASE"
        const val EXTRA_BROKER_HOST = "EXTRA_BROKER_HOST"
        const val EXTRA_BROKER_PORT = "EXTRA_BROKER_PORT"
        const val EXTRA_INTERVAL = "EXTRA_INTERVAL"

        private val _serviceState = MutableStateFlow(ServiceState())
        val serviceState = _serviceState.asStateFlow()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "DEVICE_${System.currentTimeMillis() % 10000}"

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BrutalTrack::LocationWakeLock").apply {
            setReferenceCounted(false)
        }

        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME) ?: deviceName
                channelId = intent.getStringExtra(EXTRA_CHANNEL) ?: channelId
                passphrase = intent.getStringExtra(EXTRA_PASSPHRASE) ?: passphrase
                brokerHost = intent.getStringExtra(EXTRA_BROKER_HOST) ?: brokerHost
                brokerPort = intent.getIntExtra(EXTRA_BROKER_PORT, brokerPort)
                intervalSec = intent.getIntExtra(EXTRA_INTERVAL, intervalSec)

                startTracking()
            }
            ACTION_STOP -> {
                stopTracking()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_NOTIFICATION,
                getString(R.string.tracking_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.tracking_notification_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, LocationService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID_NOTIFICATION)
            .setContentTitle("BRUTAL//TRACK: ACTIVE")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "KILL TRACKING", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        try {
            wakeLock?.acquire(24 * 60 * 60 * 1000L) // 24h max safeguard
        } catch (e: Exception) {
            Log.e(tag, "WakeLock error", e)
        }

        // Start Foreground with Location type
        val notification = buildNotification("BROADCASTING TO CHANNEL [${channelId.uppercase()}]")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        _serviceState.value = _serviceState.value.copy(
            isTracking = true,
            statusMessage = "CONNECTING TO RELAY..."
        )

        serviceScope.launch {
            mqttPublisher = MqttPublisher(brokerHost, brokerPort, channelId)
            val connected = mqttPublisher?.connect() ?: false
            if (connected) {
                _serviceState.value = _serviceState.value.copy(statusMessage = "BROADCASTING // E2EE ONLINE")
            } else {
                _serviceState.value = _serviceState.value.copy(statusMessage = "RELAY OFFLINE (RETRYING)")
            }
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            (intervalSec * 1000).toLong()
        ).apply {
            setMinUpdateIntervalMillis((intervalSec * 500).toLong())
            setWaitForAccurateLocation(false)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(tag, "Location permission missing", e)
            _serviceState.value = _serviceState.value.copy(statusMessage = "ERROR: PERMISSION DENIED")
        }
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc: Location = result.lastLocation ?: return
                processLocation(loc)
            }
        }
    }

    private fun processLocation(loc: Location) {
        val (batteryLevel, isCharging) = getBatteryInfo()

        val packet = LocationPacket(
            deviceId = deviceId,
            deviceName = deviceName,
            lat = loc.latitude,
            lon = loc.longitude,
            alt = loc.altitude,
            acc = loc.accuracy,
            spd = loc.speed,
            bearing = loc.bearing,
            battery = batteryLevel,
            isCharging = isCharging,
            timestamp = System.currentTimeMillis()
        )

        serviceScope.launch {
            try {
                // E2EE Encrypt
                val envelope = CryptoManager.encrypt(packet, passphrase)
                val envelopeJson = gson.toJson(envelope)

                // Publish
                val sent = mqttPublisher?.publish(envelopeJson) ?: false
                val count = _serviceState.value.pingsSent + if (sent) 1 else 0

                _serviceState.value = _serviceState.value.copy(
                    lastPacket = packet,
                    pingsSent = count,
                    lastPingTimestamp = packet.timestamp,
                    statusMessage = if (sent) "BROADCASTING // PING #$count SENT" else "SEND FAILED // RETRYING"
                )

                val speedKmh = (packet.spd * 3.6).toInt()
                updateNotification("LAT: %.4f LON: %.4f | %d KM/H".format(packet.lat, packet.lon, speedKmh))
            } catch (e: Exception) {
                Log.e(tag, "Error processing location packet", e)
            }
        }
    }

    private fun getBatteryInfo(): Pair<Int, Boolean> {
        val batteryStatus: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 100

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        return Pair(batteryPct, isCharging)
    }

    private fun stopTracking() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            serviceScope.launch {
                mqttPublisher?.disconnect()
                mqttPublisher = null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error stopping location updates", e)
        }

        _serviceState.value = _serviceState.value.copy(
            isTracking = false,
            statusMessage = "STANDBY // OFFLINE"
        )
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
        serviceScope.cancel()
    }
}
