package com.hironytic.moltonfkmp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun NotYetImplementedScreen(title: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().safeContentPadding()) {
        TextButton(onClick = onBack) { Text("戻る") }
        Box(
            modifier = Modifier.weight(1f).fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("$title は未実装です")
        }
    }
}
