package com.brutal.tracker.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brutal.tracker.data.TrackerConfig
import com.brutal.tracker.service.LocationService
import com.brutal.tracker.ui.components.BrutalButton
import com.brutal.tracker.ui.components.BrutalCard
import com.brutal.tracker.ui.components.BrutalPill
import com.brutal.tracker.ui.theme.AccentCyan
import com.brutal.tracker.ui.theme.AccentGreen
import com.brutal.tracker.ui.theme.AccentPurple
import com.brutal.tracker.ui.theme.AccentRose
import com.brutal.tracker.ui.theme.DarkBg
import com.brutal.tracker.ui.theme.DarkBorder
import com.brutal.tracker.ui.theme.DarkCard
import com.brutal.tracker.ui.theme.DarkText
import com.brutal.tracker.ui.theme.DarkTextMuted
import com.brutal.tracker.ui.theme.LightBg
import com.brutal.tracker.ui.theme.LightBorder
import com.brutal.tracker.ui.theme.LightCard
import com.brutal.tracker.ui.theme.LightText
import com.brutal.tracker.ui.theme.LightTextMuted
import com.brutal.tracker.update.UpdateChecker
import com.brutal.tracker.update.UpdateInfo

@Composable
fun MainScreen(
    onStartService: (TrackerConfig) -> Unit,
    onStopService: () -> Unit
) {
    val context = LocalContext.current
    val serviceState by LocationService.serviceState.collectAsState()
    val isDark = isSystemInDarkTheme()

    val screenBg = if (isDark) DarkBg else LightBg
    val textColor = if (isDark) DarkText else LightText
    val textMutedColor = if (isDark) DarkTextMuted else LightTextMuted
    val borderColor = if (isDark) DarkBorder else LightBorder
    val cardBg = if (isDark) DarkCard else LightCard

    val deviceName = "Arka's Phone"
    val passphrase = "hackme-if-you-can"
    val brokerHost = "broker.hivemq.com"
    val updateIntervalSec = 3

    val scrollState = rememberScrollState()

    // Auto-update checker state
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        updateInfo = UpdateChecker.checkForUpdates("1.1.0")
    }

    Scaffold(
        containerColor = screenBg,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Auto Update Alert Banner if a newer version is available
            updateInfo?.let { update ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentCyan.copy(alpha = 0.12f))
                        .border(1.dp, AccentCyan, RoundedCornerShape(12.dp))
                        .clickable {
                            if (!isDownloadingUpdate) {
                                isDownloadingUpdate = true
                                UpdateChecker.startDownloadAndInstall(context, update.downloadUrl, update.version)
                            }
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✨ UPDATE AVAILABLE (${update.version})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AccentCyan
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isDownloadingUpdate) "Downloading APK & launching installer..." else "Tap here to auto-update and install",
                                fontSize = 11.sp,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }
                        BrutalPill(
                            text = if (isDownloadingUpdate) "UPDATING" else "INSTALL",
                            backgroundColor = AccentCyan
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Top Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(14.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ARKA//TRACK",
                                color = textColor,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(
                                text = "v1.1.0",
                                color = textMutedColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "AES-256-GCM TELEMETRY",
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (serviceState.isTracking) {
                        BrutalPill("LIVE", AccentGreen)
                    } else {
                        BrutalPill("STANDBY", textMutedColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Broadcast Trigger Button
            if (!serviceState.isTracking) {
                BrutalButton(
                    text = "⚡ START BROADCAST",
                    onClick = {
                        val config = TrackerConfig(
                            deviceName = deviceName,
                            passphrase = passphrase,
                            brokerHost = brokerHost,
                            brokerPort = 1883,
                            updateIntervalSec = updateIntervalSec
                        )
                        onStartService(config)
                    },
                    backgroundColor = AccentCyan,
                    textColor = Color(0xFF090D16),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                BrutalButton(
                    text = "⛔ TERMINATE BROADCAST",
                    onClick = onStopService,
                    backgroundColor = AccentRose,
                    textColor = Color(0xFFFFFFFF),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Stream Message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(10.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "> " + serviceState.statusMessage,
                    color = if (serviceState.isTracking) AccentGreen else textMutedColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section Label
            Text(
                text = "TELEMETRY RADAR",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = textMutedColor,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val pkt = serviceState.lastPacket

            // Row 1: Coordinates Card (Cyan Accent)
            BrutalCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = borderColor,
                topAccentColor = AccentCyan
            ) {
                Column {
                    Text(
                        text = "COORDINATES (WGS84)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = textMutedColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val coordsText = if (pkt != null) "%.5f, %.5f".format(pkt.lat, pkt.lon) else "AWAITING GPS FIX..."
                    Text(
                        text = coordsText,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = textColor
                    )
                    if (pkt != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "BEARING: ${pkt.bearing.toInt()}° | ALT: ${pkt.alt.toInt()}M",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = textMutedColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Speedometer & Accuracy Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Speed Card (Rose Accent)
                BrutalCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = cardBg,
                    borderColor = borderColor,
                    topAccentColor = AccentRose
                ) {
                    Column {
                        Text(
                            text = "VELOCITY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = textMutedColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val speedKmh = if (pkt != null) (pkt.spd * 3.6).toInt() else 0
                        Text(
                            text = "$speedKmh KM/H",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = textColor
                        )
                    }
                }

                // Accuracy Card (Purple Accent)
                BrutalCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = cardBg,
                    borderColor = borderColor,
                    topAccentColor = AccentPurple
                ) {
                    Column {
                        Text(
                            text = "ACCURACY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = textMutedColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val accText = if (pkt != null) "±${pkt.acc.toInt()} M" else "±-- M"
                        Text(
                            text = accText,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Device Telemetry Card (Emerald Accent)
            BrutalCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = borderColor,
                topAccentColor = AccentGreen
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("BATTERY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = textMutedColor)
                        val batText = if (pkt != null) "${pkt.battery}%" + if (pkt.isCharging) " ⚡" else "" else "--%"
                        Text(batText, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 18.sp, color = textColor)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("PINGS TRANSMITTED", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = textMutedColor)
                        Text("${serviceState.pingsSent}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 18.sp, color = textColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
