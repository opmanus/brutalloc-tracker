package com.brutal.tracker.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brutal.tracker.data.TrackerConfig
import com.brutal.tracker.service.LocationService
import com.brutal.tracker.ui.components.BrutalButton
import com.brutal.tracker.ui.components.BrutalCard
import com.brutal.tracker.ui.components.BrutalInputField
import com.brutal.tracker.ui.components.BrutalPill
import com.brutal.tracker.ui.theme.AccentAmber
import com.brutal.tracker.ui.theme.AccentCyan
import com.brutal.tracker.ui.theme.AccentGreen
import com.brutal.tracker.ui.theme.AccentPurple
import com.brutal.tracker.ui.theme.AccentRose
import com.brutal.tracker.ui.theme.DarkBg
import com.brutal.tracker.ui.theme.DarkBorder
import com.brutal.tracker.ui.theme.DarkCard
import com.brutal.tracker.ui.theme.DarkCardCyan
import com.brutal.tracker.ui.theme.DarkCardGreen
import com.brutal.tracker.ui.theme.DarkCardPurple
import com.brutal.tracker.ui.theme.DarkCardRose
import com.brutal.tracker.ui.theme.DarkText
import com.brutal.tracker.ui.theme.DarkTextMuted
import com.brutal.tracker.ui.theme.LightBg
import com.brutal.tracker.ui.theme.LightBorder
import com.brutal.tracker.ui.theme.LightCard
import com.brutal.tracker.ui.theme.LightCardCyan
import com.brutal.tracker.ui.theme.LightCardGreen
import com.brutal.tracker.ui.theme.LightCardPurple
import com.brutal.tracker.ui.theme.LightCardRose
import com.brutal.tracker.ui.theme.LightText
import com.brutal.tracker.ui.theme.LightTextMuted

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

    val cardCyanBg = if (isDark) DarkCardCyan else LightCardCyan
    val cardRoseBg = if (isDark) DarkCardRose else LightCardRose
    val cardPurpleBg = if (isDark) DarkCardPurple else LightCardPurple
    val cardGreenBg = if (isDark) DarkCardGreen else LightCardGreen

    var deviceName by remember { mutableStateOf("Arka's Phone") }
    var passphrase by remember { mutableStateOf("hackme-if-you-can") }
    var brokerHost by remember { mutableStateOf("broker.hivemq.com") }
    var updateIntervalSec by remember { mutableIntStateOf(3) }

    val scrollState = rememberScrollState()

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
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(8.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ARKA//TRACK",
                                color = textColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "AES-256-GCM E2EE TELEMETRY",
                                color = AccentCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        if (serviceState.isTracking) {
                            BrutalPill("LIVE", AccentGreen)
                        } else {
                            BrutalPill("STANDBY", AccentRose)
                        }
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
                            deviceName = deviceName.trim(),
                            passphrase = passphrase.trim(),
                            brokerHost = brokerHost.trim(),
                            brokerPort = 1883,
                            updateIntervalSec = updateIntervalSec
                        )
                        onStartService(config)
                    },
                    backgroundColor = AccentCyan,
                    textColor = DarkBg,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                BrutalButton(
                    text = "⛔ TERMINATE BROADCAST",
                    onClick = onStopService,
                    backgroundColor = AccentRose,
                    textColor = DarkBg,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Stream Message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(6.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "> " + serviceState.statusMessage,
                    color = if (serviceState.isTracking) AccentGreen else textMutedColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section Label
            Text(
                text = "LIVE TELEMETRY HUD",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = textColor,
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val pkt = serviceState.lastPacket

            // Row 1: Coordinates Card
            BrutalCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardCyanBg,
                borderColor = borderColor
            ) {
                Column {
                    Text(
                        text = "COORDINATES (WGS84)",
                        fontFamily = FontFamily.SansSerif,
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
                // Speed Card
                BrutalCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = cardRoseBg,
                    borderColor = borderColor
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

                // Accuracy Card
                BrutalCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = cardPurpleBg,
                    borderColor = borderColor
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

            // Device Telemetry Card
            BrutalCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardGreenBg,
                borderColor = borderColor
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
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

            Spacer(modifier = Modifier.height(20.dp))

            // CONFIGURATION CARD
            BrutalCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = borderColor
            ) {
                Column {
                    Text(
                        text = "CONFIGURATION // ENCRYPTION",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    BrutalInputField(
                        value = deviceName,
                        onValueChange = { deviceName = it },
                        label = "Device Name (e.g. Arka's Phone / Car / Bike)"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    BrutalInputField(
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        label = "Master Passcode (Default: hackme-if-you-can)",
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    BrutalInputField(
                        value = brokerHost,
                        onValueChange = { brokerHost = it },
                        label = "MQTT Broker Relay Host"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
