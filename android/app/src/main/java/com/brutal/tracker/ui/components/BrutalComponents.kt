package com.brutal.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brutal.tracker.ui.theme.BrutalBlack
import com.brutal.tracker.ui.theme.BrutalWhite
import com.brutal.tracker.ui.theme.BrutalYellow

/**
 * Brutal Card with hard offset shadow and thick solid black border
 */
@Composable
fun BrutalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrutalYellow,
    borderColor: Color = BrutalBlack,
    borderWidth: Dp = 3.dp,
    shadowOffset: Dp = 6.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .drawBehind {
                val shadowPx = shadowOffset.toPx()
                // Draw hard offset black shadow
                drawRect(
                    color = BrutalBlack,
                    topLeft = Offset(shadowPx, shadowPx),
                    size = size
                )
            }
            .background(backgroundColor, RoundedCornerShape(0.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(0.dp))
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * Brutal Button with instant tactile drop onto hard shadow
 */
@Composable
fun BrutalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrutalWhite,
    textColor: Color = BrutalBlack,
    borderColor: Color = BrutalBlack,
    borderWidth: Dp = 3.dp,
    shadowOffset: Dp = 5.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentOffset = if (isPressed) shadowOffset else 0.dp
    val effectiveShadow = if (isPressed) 0.dp else shadowOffset

    Box(
        modifier = modifier
            .offset(x = currentOffset, y = currentOffset)
            .drawBehind {
                if (effectiveShadow > 0.dp) {
                    val shadowPx = effectiveShadow.toPx()
                    drawRect(
                        color = BrutalBlack,
                        topLeft = Offset(shadowPx, shadowPx),
                        size = size
                    )
                }
            }
            .background(backgroundColor, RoundedCornerShape(0.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(0.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // No ripple, instant brutalist press
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (-0.5).sp
        )
    }
}

/**
 * Brutal Pill Tag
 */
@Composable
fun BrutalPill(
    text: String,
    backgroundColor: Color,
    textColor: Color = BrutalBlack,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .drawBehind {
                val shadowPx = 3.dp.toPx()
                drawRect(
                    color = BrutalBlack,
                    topLeft = Offset(shadowPx, shadowPx),
                    size = size
                )
            }
            .background(backgroundColor, RoundedCornerShape(0.dp))
            .border(2.dp, BrutalBlack, RoundedCornerShape(0.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Brutal Input Box
 */
@Composable
fun BrutalInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = BrutalBlack,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 18.dp)
                .fillMaxWidth()
                .drawBehind {
                    val shadowPx = 3.dp.toPx()
                    drawRect(
                        color = BrutalBlack,
                        topLeft = Offset(shadowPx, shadowPx),
                        size = size
                    )
                }
                .background(BrutalWhite, RoundedCornerShape(0.dp))
                .border(2.dp, BrutalBlack, RoundedCornerShape(0.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                cursorBrush = SolidColor(BrutalBlack),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BrutalBlack
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
