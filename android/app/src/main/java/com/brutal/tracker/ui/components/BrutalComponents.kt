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
 * Tactical Card with clean 1px border and refined surface color
 */
@Composable
fun BrutalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DarkCard,
    borderColor: Color = DarkBorder,
    borderWidth: Dp = 1.dp,
    shadowOffset: Dp = 0.dp, // preserved for API compatibility
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(6.dp))
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * Tactical Action Button with crisp feedback and no clipping
 */
@Composable
fun BrutalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AccentCyan,
    textColor: Color = Color(0xFF090B10),
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    shadowOffset: Dp = 0.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentAlpha = if (isPressed) 0.82f else 1.0f

    Box(
        modifier = modifier
            .background(
                backgroundColor.copy(alpha = currentAlpha),
                RoundedCornerShape(6.dp)
            )
            .border(borderWidth, borderColor, RoundedCornerShape(6.dp))
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
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Tactical Pill Tag
 */
@Composable
fun BrutalPill(
    text: String,
    backgroundColor: Color,
    textColor: Color = Color(0xFF090B10),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Tactical Input Field with proper Column stacking so labels never clip
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
    val inputBg = if (isDark) Color(0xFF090B10) else LightCard
    val textCol = if (isDark) DarkText else LightText
    val labelCol = if (isDark) DarkTextMuted else LightTextMuted
    val borderCol = if (isDark) DarkBorder else LightBorder

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
                .background(inputBg, RoundedCornerShape(4.dp))
                .border(1.dp, borderCol, RoundedCornerShape(4.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                cursorBrush = SolidColor(textCol),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = textCol
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
