package com.brutal.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Subdued Tactical Palette (Fine Accents)
val AccentCyan = Color(0xFF38BDF8)
val AccentGreen = Color(0xFF34D399)
val AccentRose = Color(0xFFFB7185)
val AccentPurple = Color(0xFFC084FC)
val AccentAmber = Color(0xFFFBBF24)

// Neutral Foundation
val DarkBg = Color(0xFF090B10)
val DarkCard = Color(0xFF131722)
val DarkCardMuted = Color(0xFF181E2B)
val DarkBorder = Color(0xFF222B3D)
val DarkText = Color(0xFFE2E8F0)
val DarkTextMuted = Color(0xFF8B9CB2)

val LightBg = Color(0xFFF8FAFC)
val LightCard = Color(0xFFFFFFFF)
val LightCardMuted = Color(0xFFF1F5F9)
val LightBorder = Color(0xFFCBD5E1)
val LightText = Color(0xFF0F172A)
val LightTextMuted = Color(0xFF64748B)

// Subtle Tinted Surface Accents for Dark Mode
val DarkCardCyan = Color(0xFF0C2333)
val DarkCardGreen = Color(0xFF0D2820)
val DarkCardRose = Color(0xFF291218)
val DarkCardPurple = Color(0xFF221330)
val DarkCardAmber = Color(0xFF261D10)

// Subtle Tinted Surface Accents for Light Mode
val LightCardCyan = Color(0xFFF0F9FF)
val LightCardGreen = Color(0xFFF0FDF4)
val LightCardRose = Color(0xFFFFF1F2)
val LightCardPurple = Color(0xFFFAF5FF)
val LightCardAmber = Color(0xFFFEFCE8)

// Legacy aliases for safe builds
val BrutalRed = AccentRose
val BrutalBlue = AccentCyan
val BrutalYellow = AccentAmber
val BrutalGreen = AccentGreen
val BrutalMagenta = AccentPurple
val BrutalBlack = Color(0xFF090B10)
val BrutalWhite = Color(0xFFFFFFFF)
val BrutalDarkBg = DarkBg
val BrutalDarkCard = DarkCard
val BrutalDarkCardYellow = DarkCardAmber
val BrutalDarkCardRed = DarkCardRose
val BrutalDarkCardMagenta = DarkCardPurple
val BrutalDarkCardGreen = DarkCardGreen
val BrutalDarkBorder = DarkBorder
val BrutalLightBg = LightBg
val BrutalLightCard = LightCard
val BrutalLightCardYellow = LightCardAmber
val BrutalLightCardRed = LightCardRose
val BrutalLightCardMagenta = LightCardPurple
val BrutalLightCardGreen = LightCardGreen

val BrutalShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

val BrutalTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = AccentCyan,
    onPrimary = Color(0xFF0F172A),
    secondary = AccentGreen,
    onSecondary = Color(0xFF0F172A),
    tertiary = AccentRose,
    background = LightBg,
    onBackground = LightText,
    surface = LightCard,
    onSurface = LightText
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color(0xFF090B10),
    secondary = AccentGreen,
    onSecondary = Color(0xFF090B10),
    tertiary = AccentRose,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkCard,
    onSurface = DarkText
)

@Composable
fun BrutalTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = BrutalTypography,
        shapes = BrutalShapes,
        content = content
    )
}
