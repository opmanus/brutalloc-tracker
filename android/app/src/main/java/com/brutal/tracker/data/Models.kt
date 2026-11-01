package com.brutal.tracker.data

import com.google.gson.annotations.SerializedName
import java.security.MessageDigest

/**
 * Plaintext location packet before encryption
 */
data class LocationPacket(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("deviceName") val deviceName: String = "Arka's Phone",
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("alt") val alt: Double,
    @SerializedName("acc") val acc: Float,
    @SerializedName("spd") val spd: Float,
    @SerializedName("bearing") val bearing: Float,
    @SerializedName("battery") val battery: Int,
    @SerializedName("isCharging") val isCharging: Boolean,
    @SerializedName("timestamp") val timestamp: Long
)

/**
 * Encrypted envelope matching browser Web Crypto Subtle format:
 * s  -> 16-byte salt (Base64)
 * iv -> 12-byte IV (Base64)
 * c  -> ciphertext + auth tag (Base64)
 */
data class EncryptedEnvelope(
    @SerializedName("s") val s: String,
    @SerializedName("iv") val iv: String,
    @SerializedName("c") val c: String
)

/**
 * User persistent settings with deterministic auto-channel derivation
 */
data class TrackerConfig(
    val deviceName: String = "Arka's Phone",
    val passphrase: String = "arka-secret-pass",
    val brokerHost: String = "broker.hivemq.com",
    val brokerPort: Int = 1883,
    val updateIntervalSec: Int = 3
) {
    /**
     * Auto-derived channel so user never has to manually enter or sync channel IDs
     */
    val derivedChannel: String
        get() {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(("arka-mandal-" + passphrase.trim()).toByteArray(Charsets.UTF_8))
            return hash.joinToString("") { "%02x".format(it) }.take(16)
        }
}

/**
 * Real-time service state for Jetpack Compose UI
 */
data class ServiceState(
    val isTracking: Boolean = false,
    val lastPacket: LocationPacket? = null,
    val pingsSent: Int = 0,
    val lastPingTimestamp: Long = 0L,
    val statusMessage: String = "STANDBY // READY"
)
