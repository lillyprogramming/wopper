package at.uastw.fishdiary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB0D0D3),
    onPrimary = Color(0xFFC08497),

    secondary = DeepBlue,
    onSecondary = White,

    background = Color(0xFFF7AF9D),
    onBackground = White,

    surface = Color(0xFFF7AF9D),
    onSurface = Color(0xFF000000),

    outline = White,
)

private val DarkColorScheme = darkColorScheme(
    primary = NavyHeader,
    onPrimary = White,

    secondary = DeepBlue,
    onSecondary = White,

    background = Color(0xFF0B1C2A),
    onBackground = White,

    surface = Color(0xFF10293B),
    onSurface = White,

    outline = Color(0xFF2E6C91)
)

@Composable
fun FishDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
