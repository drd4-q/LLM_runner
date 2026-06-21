package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElegantLavender,
    secondary = SnapdragonRed,
    tertiary = ElegantLavender,
    background = DeepCharcoalBg,
    surface = SlateCardBg,
    onPrimary = ActivePurple,
    onSecondary = CozyPureWhite,
    onBackground = LightSlateText,
    onSurface = LightSlateText,
    outline = SlateOutline
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
