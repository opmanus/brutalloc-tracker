package com.brutal.tracker.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttPublisher(
    private val host: String,
    private val port: Int,
    private val channelId: String,
    private val deviceId: String = "DEV"
) {
    private val tag = "MqttPublisher"
    private var client: MqttClient? = null
    private val baseTopic = "brutalloc/$channelId"
    private val deviceTopic = "brutalloc/$channelId/$deviceId"

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            val serverUri = "tcp://$host:$port"
            val clientId = "brutal_android_" + System.currentTimeMillis() % 1000000

            client = MqttClient(serverUri, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 10
                keepAliveInterval = 30
                isAutomaticReconnect = true
            }

            Log.d(tag, "Connecting to MQTT broker: $serverUri on topic: $deviceTopic")
            client?.connect(options)
            Log.d(tag, "Connected successfully to $serverUri")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to connect to MQTT broker", e)
            false
        }
    }

    suspend fun publish(payloadJson: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (client == null || client?.isConnected != true) {
                Log.w(tag, "Client disconnected, attempting reconnect...")
                connect()
            }

            // Retained message with QoS 1 ensures MQTT broker keeps latest location on server
            // so the web dashboard receives the phone immediately upon loading!
            val message = MqttMessage(payloadJson.toByteArray(Charsets.UTF_8)).apply {
                qos = 1
                isRetained = true
            }

            client?.publish(deviceTopic, message)
            client?.publish(baseTopic, message)
            Log.d(tag, "Published retained packet to $deviceTopic and $baseTopic (${payloadJson.length} bytes)")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to publish packet", e)
            false
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            if (client?.isConnected == true) {
                client?.disconnect()
            }
            client?.close()
            client = null
            Log.d(tag, "Disconnected from MQTT broker.")
        } catch (e: Exception) {
            Log.e(tag, "Error disconnecting MQTT client", e)
        }
    }
}
