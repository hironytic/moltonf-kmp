package com.hironytic.moltonfkmp.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A top bar like the navigation bar of moltonf-web: black with a thin page-colored border below.
 */
@Composable
fun MoltonfTopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
) {
    Column {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                titleContentColor = Palette.Gray300,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.background)
    }
}

/**
 * A subdued outlined button (gray text and border), used for "back"/"cancel" kinds of actions.
 */
@Composable
fun NeutralOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(text)
    }
}

/**
 * A heading of a screen's content, like the header title of moltonf-web.
 */
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.inverseSurface,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineSmall,
    )
}

/**
 * Colors of a text field like the input of moltonf-web: gray filled, white text, and a colored
 * border only while focused.
 */
@Composable
fun moltonfTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    focusedTextColor = Palette.White,
    unfocusedTextColor = Palette.White,
    focusedBorderColor = Palette.Primary500,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    cursorColor = Palette.White,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface,
)
