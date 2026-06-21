package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepCharcoalBg
import com.example.ui.theme.HexagonTeal
import com.example.ui.theme.SnapdragonRed
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTransition: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val fadeAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "fadeAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2200) // Beautiful splash showing Snapdragon NPU design
        onTransition()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCharcoalBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // High-fidelity Snapdragon Hexagon NPU microchip drawn using Canvas
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2
                    val cy = h / 2

                    // Draw outer processor outline
                    drawRoundRect(
                        color = HexagonTeal.copy(alpha = fadeAlpha * 0.4f),
                        topLeft = Offset(w * 0.1f, h * 0.1f),
                        size = Size(w * 0.8f, h * 0.8f),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw silicon chip surface
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SnapdragonRed.copy(alpha = fadeAlpha * 0.7f),
                                DeepCharcoalBg
                            ),
                            center = Offset(cx, cy),
                            radius = cx * 0.9f
                        ),
                        topLeft = Offset(w * 0.2f, h * 0.2f),
                        size = Size(w * 0.6f, h * 0.6f),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )

                    // Draw gold pin connections (NPU bus lines)
                    val pinLength = 12.dp.toPx()
                    val pinWidth = 2.dp.toPx()
                    
                    // Pins top and bottom
                    for (i in 0..5) {
                        val px = w * 0.25f + i * (w * 0.5f / 5)
                        // Top pins
                        drawLine(
                            color = HexagonTeal.copy(alpha = fadeAlpha),
                            start = Offset(px, h * 0.1f),
                            end = Offset(px, h * 0.1f - pinLength),
                            strokeWidth = pinWidth
                        )
                        // Bottom pins
                        drawLine(
                            color = HexagonTeal.copy(alpha = fadeAlpha),
                            start = Offset(px, h * 0.9f),
                            end = Offset(px, h * 0.9f + pinLength),
                            strokeWidth = pinWidth
                        )
                    }

                    // Pins left and right
                    for (i in 0..5) {
                        val py = h * 0.25f + i * (h * 0.5f / 5)
                        // Left pins
                        drawLine(
                            color = HexagonTeal.copy(alpha = fadeAlpha),
                            start = Offset(w * 0.1f, py),
                            end = Offset(w * 0.1f - pinLength, py),
                            strokeWidth = pinWidth
                        )
                        // Right pins
                        drawLine(
                            color = HexagonTeal.copy(alpha = fadeAlpha),
                            start = Offset(w * 0.9f, py),
                            end = Offset(w * 0.9f + pinLength, py),
                            strokeWidth = pinWidth
                        )
                    }

                    // Inner Hexagon NPU Core
                    drawCircle(
                        color = HexagonTeal.copy(alpha = fadeAlpha * pulseScale),
                        radius = 18.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    drawCircle(
                        color = SnapdragonRed.copy(alpha = fadeAlpha),
                        radius = 8.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "QUALCOMM AI ENGINE",
                fontSize = 13.sp,
                color = SnapdragonRed,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "QNN-LLM-Runner",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "NPU LOCAL SPEED ACCELERATOR",
                fontSize = 11.sp,
                color = HexagonTeal.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
