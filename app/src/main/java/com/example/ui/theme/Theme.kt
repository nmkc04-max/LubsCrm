package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDeepBlue,
    secondary = SecondaryPremiumGold,
    tertiary = SecondaryPremiumGold,
    background = Color(0xFF0F172A), // Slate 900
    surface = Color(0xFF1E293B), // Slate 800
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color.White,
    outline = Color(0xFF475569),
    error = AlertRed
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDeepBlue,
    secondary = SecondaryPremiumGold,
    tertiary = PrimaryDeepBlue,
    background = LightBackground,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = TextPrimary,
    onTertiary = PureWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = LightBackground,
    onSurfaceVariant = TextSecondary,
    outline = LightGrayBorder,
    error = AlertRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable to force our custom premium brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
