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

// Refined Neo-Brutalist Palette (Balanced & High Contrast)
val BrutalRed = Color(0xFFEF4444)
val BrutalBlue = Color(0xFF38BDF8)
val BrutalYellow = Color(0xFFF59E0B)
val BrutalGreen = Color(0xFF10B981)
val BrutalMagenta = Color(0xFFC026D3)

val BrutalBlack = Color(0xFF000000)
val BrutalWhite = Color(0xFFFFFFFF)

// Dark Theme Surfaces
val BrutalDarkBg = Color(0xFF0D0F14)
val BrutalDarkCard = Color(0xFF161920)
val BrutalDarkCardYellow = Color(0xFF201D14)
val BrutalDarkCardRed = Color(0xFF281416)
val BrutalDarkCardMagenta = Color(0xFF27142B)
val BrutalDarkCardGreen = Color(0xFF12261B)
val BrutalDarkBorder = Color(0xFF3B4252)

// Light Theme Surfaces
val BrutalLightBg = Color(0xFFF4F4F0)
val BrutalLightCard = Color(0xFFFFFFFF)
val BrutalLightCardYellow = Color(0xFFFEF9C3)
val BrutalLightCardRed = Color(0xFFFEE2E2)
val BrutalLightCardMagenta = Color(0xFFFAE8FF)
val BrutalLightCardGreen = Color(0xFFD1FAE5)

val BrutalShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

val BrutalTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 38.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 22.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = BrutalBlack,
    onPrimary = BrutalWhite,
    secondary = BrutalYellow,
    onSecondary = BrutalBlack,
    tertiary = BrutalRed,
    background = BrutalLightBg,
    onBackground = BrutalBlack,
    surface = BrutalLightCard,
    onSurface = BrutalBlack
)

private val DarkColorScheme = darkColorScheme(
    primary = BrutalWhite,
    onPrimary = BrutalBlack,
    secondary = BrutalYellow,
    onSecondary = BrutalBlack,
    tertiary = BrutalRed,
    background = BrutalDarkBg,
    onBackground = BrutalWhite,
    surface = BrutalDarkCard,
    onSurface = BrutalWhite
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
