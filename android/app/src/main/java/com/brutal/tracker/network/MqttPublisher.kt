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
    private val channelId: String
) {
    private val tag = "MqttPublisher"
    private var client: MqttClient? = null
    private val topic = "brutalloc/$channelId"

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

            Log.d(tag, "Connecting to MQTT broker: $serverUri on topic: $topic")
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

            val message = MqttMessage(payloadJson.toByteArray(Charsets.UTF_8)).apply {
                qos = 0
                isRetained = false
            }
            client?.publish(topic, message)
            Log.d(tag, "Published encrypted packet to topic $topic (${payloadJson.length} bytes)")
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
