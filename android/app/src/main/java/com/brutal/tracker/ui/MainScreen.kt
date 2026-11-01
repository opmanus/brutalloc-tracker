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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import com.brutal.tracker.ui.theme.BrutalBlack
import com.brutal.tracker.ui.theme.BrutalBlue
import com.brutal.tracker.ui.theme.BrutalDarkBg
import com.brutal.tracker.ui.theme.BrutalDarkBorder
import com.brutal.tracker.ui.theme.BrutalDarkCard
import com.brutal.tracker.ui.theme.BrutalDarkCardGreen
import com.brutal.tracker.ui.theme.BrutalDarkCardMagenta
import com.brutal.tracker.ui.theme.BrutalDarkCardRed
import com.brutal.tracker.ui.theme.BrutalDarkCardYellow
import com.brutal.tracker.ui.theme.BrutalGreen
import com.brutal.tracker.ui.theme.BrutalLightBg
import com.brutal.tracker.ui.theme.BrutalLightCard
import com.brutal.tracker.ui.theme.BrutalLightCardGreen
import com.brutal.tracker.ui.theme.BrutalLightCardMagenta
import com.brutal.tracker.ui.theme.BrutalLightCardRed
import com.brutal.tracker.ui.theme.BrutalLightCardYellow
import com.brutal.tracker.ui.theme.BrutalMagenta
import com.brutal.tracker.ui.theme.BrutalRed
import com.brutal.tracker.ui.theme.BrutalWhite
import com.brutal.tracker.ui.theme.BrutalYellow

@Composable
fun MainScreen(
    onStartService: (TrackerConfig) -> Unit,
    onStopService: () -> Unit
) {
    val context = LocalContext.current
    val serviceState by LocationService.serviceState.collectAsState()
    val isDark = isSystemInDarkTheme()

    val screenBg = if (isDark) BrutalDarkBg else BrutalLightBg
    val textColor = if (isDark) BrutalWhite else BrutalBlack
    val borderColor = if (isDark) BrutalDarkBorder else BrutalBlack
    val cardBg = if (isDark) BrutalDarkCard else BrutalLightCard
    val yellowCardBg = if (isDark) BrutalDarkCardYellow else BrutalLightCardYellow
    val redCardBg = if (isDark) BrutalDarkCardRed else BrutalLightCardRed
    val magentaCardBg = if (isDark) BrutalDarkCardMagenta else BrutalLightCardMagenta
    val greenCardBg = if (isDark) BrutalDarkCardGreen else BrutalLightCardGreen

    var channelId by remember { mutableStateOf("alpha-squad-01") }
    var passphrase by remember { mutableStateOf("hackme-if-you-can") }
    var brokerHost by remember { mutableStateOf("broker.hivemq.com") }
    var updateIntervalSec by remember { mutableIntStateOf(3) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Brutalist Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val shadowPx = 5.dp.toPx()
                    drawRect(BrutalBlack, Offset(shadowPx, shadowPx), size)
                }
                .background(if (isDark) BrutalDarkCard else BrutalBlack, RoundedCornerShape(0.dp))
                .border(3.dp, borderColor, RoundedCornerShape(0.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BRUTAL//TRACK",
                        color = BrutalWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )

                    if (serviceState.isTracking) {
                        BrutalPill("LIVE", BrutalGreen, BrutalBlack)
                    } else {
                        BrutalPill("OFFLINE", BrutalRed, BrutalBlack)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AES-256-GCM E2EE GPS STREAMER",
                    color = BrutalYellow,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Action Button
        if (!serviceState.isTracking) {
            BrutalButton(
                text = "⚡ START BROADCAST",
                onClick = {
                    val config = TrackerConfig(
                        channelId = channelId.trim(),
                        passphrase = passphrase.trim(),
                        brokerHost = brokerHost.trim(),
                        brokerPort = 1883,
                        updateIntervalSec = updateIntervalSec
                    )
                    onStartService(config)
                },
                backgroundColor = BrutalYellow,
                textColor = BrutalBlack,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            BrutalButton(
                text = "⛔ TERMINATE BROADCAST",
                onClick = onStopService,
                backgroundColor = BrutalRed,
                textColor = BrutalWhite,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) BrutalDarkCard else BrutalBlack)
                .border(2.dp, borderColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "> " + serviceState.statusMessage,
                color = if (serviceState.isTracking) BrutalGreen else BrutalYellow,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TELEMETRY HUD
        Text(
            text = "TELEMETRY HUD",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = textColor,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        val pkt = serviceState.lastPacket

        // Row 1: Coordinates Card
        BrutalCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = cardBg,
            borderColor = borderColor,
            shadowOffset = 4.dp
        ) {
            Column {
                Text(
                    text = "COORDINATES (WGS84)",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                val coordsText = if (pkt != null) "%.5f, %.5f".format(pkt.lat, pkt.lon) else "WAITING FOR GPS..."
                Text(
                    text = coordsText,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = textColor
                )
                if (pkt != null) {
                    Text(
                        text = "BEARING: ${pkt.bearing.toInt()}° | ALT: ${pkt.alt.toInt()}M",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Speedometer & Accuracy Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Speed Card
            BrutalCard(
                modifier = Modifier.weight(1f),
                backgroundColor = redCardBg,
                borderColor = borderColor,
                shadowOffset = 4.dp
            ) {
                Column {
                    Text(
                        text = "VELOCITY",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = textColor
                    )
                    val speedKmh = if (pkt != null) (pkt.spd * 3.6).toInt() else 0
                    Text(
                        text = "$speedKmh KM/H",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = textColor
                    )
                }
            }

            // Accuracy Card
            BrutalCard(
                modifier = Modifier.weight(1f),
                backgroundColor = magentaCardBg,
                borderColor = borderColor,
                shadowOffset = 4.dp
            ) {
                Column {
                    Text(
                        text = "GPS ACCURACY",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = textColor
                    )
                    val accText = if (pkt != null) "±${pkt.acc.toInt()} M" else "±-- M"
                    Text(
                        text = accText,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = textColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Device Telemetry Card
        BrutalCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = greenCardBg,
            borderColor = borderColor,
            shadowOffset = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("DEVICE BATTERY", fontWeight = FontWeight.Black, fontSize = 11.sp, color = textColor)
                    val batText = if (pkt != null) "${pkt.battery}%" + if (pkt.isCharging) " (⚡)" else "" else "--%"
                    Text(batText, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 18.sp, color = textColor)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL PINGS", fontWeight = FontWeight.Black, fontSize = 11.sp, color = textColor)
                    Text("${serviceState.pingsSent}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 18.sp, color = textColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CONFIGURATION CARD
        BrutalCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = yellowCardBg,
            borderColor = borderColor,
            shadowOffset = 5.dp
        ) {
            Column {
                Text(
                    text = "SETTINGS // ENCRYPTION",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(12.dp))

                BrutalInputField(
                    value = channelId,
                    onValueChange = { channelId = it },
                    label = "Channel ID (Share with your PC)"
                )

                Spacer(modifier = Modifier.height(10.dp))

                BrutalInputField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = "AES-256 Secret Passphrase",
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                BrutalInputField(
                    value = brokerHost,
                    onValueChange = { brokerHost = it },
                    label = "MQTT Broker Host"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // PC SYNC LINK CARD
        BrutalCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = cardBg,
            borderColor = borderColor,
            shadowOffset = 5.dp
        ) {
            Column {
                Text(
                    text = "DESKTOP / PC SYNC",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Open the web dashboard on your PC or other devices to view live decrypted tracking on a tactical map.",
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                BrutalButton(
                    text = "SHARE PC DASHBOARD LINK",
                    onClick = {
                        val shareUrl = "https://opmanus.github.io/brutalloc-tracker/?channel=${channelId}&pass=${passphrase}"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Open BrutalTrack on PC: $shareUrl")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Live Track Link"))
                    },
                    backgroundColor = BrutalYellow,
                    textColor = BrutalBlack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
