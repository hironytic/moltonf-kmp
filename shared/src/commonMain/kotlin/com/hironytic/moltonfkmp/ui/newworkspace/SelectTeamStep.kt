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
fun SelectTeamStep(viewModel: NewWorkspaceViewModel) {
    val teamOptions by viewModel.teamOptions.collectAsState()
    val team by viewModel.team.collectAsState()
    val canForward by viewModel.canForwardFromSelectTeamStep.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("どの視点で観戦しますか？", style = MaterialTheme.typography.headlineSmall)
        Text("エピローグになるまでは、選んだ視点に合わせて表示されるものが変わります。例えば、村人の視点では人狼たちのささやきは表示されません。")
        Text("村人、人狼を選択した場合は、続けて次の画面で詳細を選択できます。")

        OptionChooser(
            options = teamOptions,
            selected = team,
            onChoose = { viewModel.selectTeam(it) },
        ) { option ->
            when (option) {
                TeamOption.VILLAGER -> OptionContent("村人", "村人側の視点で観戦します。")
                TeamOption.WOLF -> OptionContent("人狼", "人狼側の視点で観戦します。狂人もこちらに含みます。")
                TeamOption.HAMSTER -> OptionContent("ハムスター人間", "ハムスター人間の視点で観戦します。")
                TeamOption.ANYTHING -> OptionContent("おまかせ", "観戦データ作成時にシステムがランダムに決定します。")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { viewModel.backFromSelectTeamStep() }) { Text("戻る") }
            Button(onClick = { viewModel.forwardFromSelectTeamStep() }, enabled = canForward) { Text("次へ") }
        }
    }
}
