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

// Modern Tactical Accents
val AccentCyan = Color(0xFF38BDF8)
val AccentGreen = Color(0xFF34D399)
val AccentRose = Color(0xFFFB7185)
val AccentPurple = Color(0xFFC084FC)
val AccentAmber = Color(0xFFFBBF24)

// Dark Theme Foundations (Unified & Elegant)
val DarkBg = Color(0xFF090D16)
val DarkCard = Color(0xFF111827)
val DarkBorder = Color(0xFF1E293B)
val DarkText = Color(0xFFF8FAFC)
val DarkTextMuted = Color(0xFF8492A6)

// Light Theme Foundations
val LightBg = Color(0xFFF1F5F9)
val LightCard = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFE2E8F0)
val LightText = Color(0xFF0F172A)
val LightTextMuted = Color(0xFF64748B)

// Card Surface Accents
val DarkCardCyan = DarkCard
val DarkCardGreen = DarkCard
val DarkCardRose = DarkCard
val DarkCardPurple = DarkCard
val DarkCardAmber = DarkCard

val LightCardCyan = LightCard
val LightCardGreen = LightCard
val LightCardRose = LightCard
val LightCardPurple = LightCard
val LightCardAmber = LightCard

// Legacy aliases for backward compatibility
val BrutalRed = AccentRose
val BrutalBlue = AccentCyan
val BrutalYellow = AccentAmber
val BrutalGreen = AccentGreen
val BrutalMagenta = AccentPurple
val BrutalBlack = DarkBg
val BrutalWhite = Color(0xFFFFFFFF)
val BrutalDarkBg = DarkBg
val BrutalDarkCard = DarkCard
val BrutalDarkCardYellow = DarkCard
val BrutalDarkCardRed = DarkCard
val BrutalDarkCardMagenta = DarkCard
val BrutalDarkCardGreen = DarkCard
val BrutalDarkBorder = DarkBorder
val BrutalLightBg = LightBg
val BrutalLightCard = LightCard
val BrutalLightCardYellow = LightCard
val BrutalLightCardRed = LightCard
val BrutalLightCardMagenta = LightCard
val BrutalLightCardGreen = LightCard

val BrutalShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

val BrutalTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = AccentCyan,
    onPrimary = Color(0xFFFFFFFF),
    secondary = AccentGreen,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = AccentRose,
    background = LightBg,
    onBackground = LightText,
    surface = LightCard,
    onSurface = LightText
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color(0xFF090D16),
    secondary = AccentGreen,
    onSecondary = Color(0xFF090D16),
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
