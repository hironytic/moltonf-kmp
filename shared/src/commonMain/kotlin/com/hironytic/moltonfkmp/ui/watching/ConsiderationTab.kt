package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.ui.theme.moltonfTextFieldColors

/** The consideration screen. Currently it holds only the free-form consideration memo. */
@Composable
fun ConsiderationTab(
    viewModel: WatchingViewModel,
    modifier: Modifier = Modifier,
) {
    // Local text state keeps typing (and the IME cursor) in sync; each change is also saved.
    var memo by remember { mutableStateOf(viewModel.memo) }
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "考察メモ",
            color = MaterialTheme.colorScheme.inverseSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            value = memo,
            onValueChange = {
                memo = it
                viewModel.updateMemo(it)
            },
            placeholder = { Text("自由に入力できます") },
            colors = moltonfTextFieldColors(),
            // The field fills the remaining height and scrolls its own content, so that it keeps the
            // cursor in view while typing (which it does not do when it grows inside a scrolling parent).
            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp),
        )
    }
}
