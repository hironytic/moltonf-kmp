package com.hironytic.moltonfkmp.ui.newworkspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun <T> OptionChooser(
    options: List<T>,
    selected: T?,
    onChoose: (T) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (option in options) {
            val isSelected = option == selected
            OutlinedButton(
                onClick = { onChoose(option) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                ),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    content(option)
                }
            }
        }
    }
}

@Composable
fun OptionContent(title: String, description: String) {
    Column {
        Text(title, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.bodySmall)
    }
}
