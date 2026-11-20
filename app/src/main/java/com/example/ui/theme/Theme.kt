package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AmarNoteLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
    secondary = PurpleSecondary,
    onSecondary = PurpleOnSecondary,
    secondaryContainer = PurpleContainer,
    onSecondaryContainer = PurpleOnContainer,
    tertiary = OrangeTertiary,
    onTertiary = OrangeOnTertiary,
    tertiaryContainer = OrangeContainer,
    onTertiaryContainer = OrangeOnContainer,
    background = IvoryBackground,
    onBackground = InkOnBackground,
    surface = PaperSurface,
    onSurface = InkOnBackground,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = InkMuted,
    outline = OutlineSubtle
)

private val AmarNoteDarkColorScheme = darkColorScheme(
    primary = EmeraldDarkPrimary,
    onPrimary = EmeraldDarkOnPrimary,
    primaryContainer = EmeraldDarkContainer,
    onPrimaryContainer = EmeraldDarkOnContainer,
    secondary = PurpleDarkSecondary,
    onSecondary = PurpleDarkOnSecondary,
    secondaryContainer = PurpleDarkContainer,
    onSecondaryContainer = PurpleDarkOnContainer,
    tertiary = OrangeDarkTertiary,
    onTertiary = OrangeDarkOnTertiary,
    tertiaryContainer = OrangeDarkContainer,
    onTertiaryContainer = OrangeDarkOnContainer,
    background = SlateDarkBackground,
    onBackground = LightInkOnDark,
    surface = SlateDarkSurface,
    onSurface = LightInkOnDark,
    surfaceVariant = SlateDarkSurfaceVariant,
    onSurfaceVariant = MutedInkOnDark,
    outline = OutlineDark
)

val AmarNoteShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AmarNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AmarNoteDarkColorScheme else AmarNoteLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AmarNoteTypography,
        shapes = AmarNoteShapes,
        content = content
    )
}
