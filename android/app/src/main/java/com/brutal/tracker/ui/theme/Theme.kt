package com.brutal.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Neo-Brutalist Core Palette
val BrutalRed = Color(0xFFFF0000)
val BrutalBlue = Color(0xFF0000FF)
val BrutalYellow = Color(0xFFFFFF00)
val BrutalBlack = Color(0xFF000000)
val BrutalWhite = Color(0xFFFFFFFF)
val BrutalGreen = Color(0xFF00FF00)
val BrutalMagenta = Color(0xFFFF00FF)

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
    background = BrutalWhite,
    onBackground = BrutalBlack,
    surface = BrutalYellow,
    onSurface = BrutalBlack
)

@Composable
fun BrutalTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = BrutalTypography,
        shapes = BrutalShapes,
        content = content
    )
}
