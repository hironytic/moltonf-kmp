package com.hironytic.moltonfkmp.ui.newworkspace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConfirmStep(viewModel: NewWorkspaceViewModel) {
    val name by viewModel.name.collectAsState()
    val registering by viewModel.registering.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("観戦データ「$name」を登録します。", style = MaterialTheme.typography.headlineSmall)
        Text("選択した役職に応じてあなたが着目するキャラクターが選ばれます。選ばれたキャラクターは、1日目の先頭で明らかになります。")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { viewModel.backFromConfirmStep() }, enabled = !registering) { Text("戻る") }
            Button(
                onClick = { viewModel.registerNewWorkspace() },
                enabled = !registering,
            ) { Text("登録してプロローグへ") }
        }
    }
}
