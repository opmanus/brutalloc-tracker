package com.brutal.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brutal.tracker.ui.theme.AccentCyan
import com.brutal.tracker.ui.theme.DarkBorder
import com.brutal.tracker.ui.theme.DarkCard
import com.brutal.tracker.ui.theme.DarkText
import com.brutal.tracker.ui.theme.DarkTextMuted
import com.brutal.tracker.ui.theme.LightBorder
import com.brutal.tracker.ui.theme.LightCard
import com.brutal.tracker.ui.theme.LightText
import com.brutal.tracker.ui.theme.LightTextMuted

/**
 * Modern Card with optional top accent strip and smooth 12dp rounded corners
 */
@Composable
fun BrutalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DarkCard,
    borderColor: Color = DarkBorder,
    borderWidth: Dp = 1.dp,
    topAccentColor: Color? = null,
    shadowOffset: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor, shape)
            .border(borderWidth, borderColor, shape)
    ) {
        if (topAccentColor != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(topAccentColor)
            )
        }
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

/**
 * Modern Tactical Action Button with smooth feedback
 */
@Composable
fun BrutalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AccentCyan,
    textColor: Color = Color(0xFF090D16),
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    shadowOffset: Dp = 0.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(10.dp)
    val alpha = if (isPressed) 0.82f else 1.0f

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor.copy(alpha = alpha), shape)
            .border(borderWidth, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Pill Tag with 20dp smooth rounded capsule
 */
@Composable
fun BrutalPill(
    text: String,
    backgroundColor: Color,
    textColor: Color = Color(0xFF090D16),
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .background(backgroundColor.copy(alpha = 0.15f), shape)
            .border(1.dp, backgroundColor, shape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = backgroundColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Tactical Input Field (preserved for settings dialog if needed)
 */
@Composable
fun BrutalInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val inputBg = if (isDark) Color(0xFF0C101A) else LightCard
    val textCol = if (isDark) DarkText else LightText
    val labelCol = if (isDark) DarkTextMuted else LightTextMuted
    val borderCol = if (isDark) DarkBorder else LightBorder
    val shape = RoundedCornerShape(8.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = labelCol
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(inputBg, shape)
                .border(1.dp, borderCol, shape)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                cursorBrush = SolidColor(textCol),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = textCol
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
