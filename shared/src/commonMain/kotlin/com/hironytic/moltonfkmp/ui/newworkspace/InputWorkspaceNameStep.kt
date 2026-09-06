package com.hironytic.moltonfkmp.ui.newworkspace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InputWorkspaceNameStep(viewModel: NewWorkspaceViewModel) {
    val name by viewModel.name.collectAsState()
    val canForward by viewModel.canForwardFromInputNameStep.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("観戦データの名前", style = MaterialTheme.typography.headlineSmall)
        Text("この観戦データに後で自分が見てわかりやすい名前を付けてください。")

        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.updateName(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { viewModel.backFromInputNameStep() }) { Text("戻る") }
            Button(
                onClick = { viewModel.forwardFromInputNameStep() },
                enabled = canForward,
            ) { Text("次へ") }
        }
    }
}
