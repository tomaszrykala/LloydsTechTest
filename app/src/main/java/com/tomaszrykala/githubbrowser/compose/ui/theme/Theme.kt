package com.tomaszrykala.githubbrowser.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val primaryVariant = Purple700

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = primaryVariant,
    secondary = Teal200
)

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = primaryVariant,
    secondary = Teal200

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

object GithubBrowserTheme {
    val dimens: Dimens
        @Composable
        get() = Dimens()

    @Immutable
    data class Dimens(
        val spacingTiny: Dp = 2.dp,
        val spacingSmall: Dp = 4.dp,
        val spacingStandard: Dp = 8.dp,
        val spacingSemiLarge: Dp = 12.dp,
        val spacingLarge: Dp = 16.dp,
        val spacingSemiXLarge: Dp = 24.dp,
        val spacingXLarge: Dp = 32.dp,
        val spacingSemiXXLarge: Dp = 48.dp,
        val spacingXXLarge: Dp = 64.dp,
    )
}

@Composable
fun LloydsTechTestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}