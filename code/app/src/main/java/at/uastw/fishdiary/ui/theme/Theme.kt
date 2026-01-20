package at.uastw.fishdiary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val wopperColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = DarkPink,

    secondary = Blue,
    onSecondary = White,

    background = Melon,
    onBackground = White,

    surface = Melon,
    onSurface = Black,

    outline = White,
)

@Composable
fun FishDiaryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme =  wopperColorScheme,
        typography = Typography,
        content = content
    )
}
