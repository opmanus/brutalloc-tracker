package com.brutal.tracker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.brutal.tracker.data.TrackerConfig
import com.brutal.tracker.service.LocationService
import com.brutal.tracker.ui.theme.BrutalTrackTheme

class MainActivity : ComponentActivity() {

    private var pendingConfig: TrackerConfig? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            pendingConfig?.let { startTrackingService(it) }
        } else {
            Toast.makeText(this, "Location permission is required for tracking", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BrutalTrackTheme {
                MainScreen(
                    onStartService = { config ->
                        pendingConfig = config
                        checkAndRequestPermissions()
                    },
                    onStopService = {
                        stopTrackingService()
                    }
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            pendingConfig?.let { startTrackingService(it) }
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun startTrackingService(config: TrackerConfig) {
        val intent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            putExtra(LocationService.EXTRA_DEVICE_NAME, config.deviceName)
            putExtra(LocationService.EXTRA_CHANNEL, config.derivedChannel)
            putExtra(LocationService.EXTRA_PASSPHRASE, config.passphrase)
            putExtra(LocationService.EXTRA_BROKER_HOST, config.brokerHost)
            putExtra(LocationService.EXTRA_BROKER_PORT, config.brokerPort)
            putExtra(LocationService.EXTRA_INTERVAL, config.updateIntervalSec)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopTrackingService() {
        val intent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        startService(intent)
    }
}
