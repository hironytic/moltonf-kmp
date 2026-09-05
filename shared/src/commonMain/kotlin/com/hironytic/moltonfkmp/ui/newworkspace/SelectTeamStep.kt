package com.hironytic.moltonfkmp.ui.newworkspace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("観戦する陣営を選んでください", style = MaterialTheme.typography.headlineSmall)

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

        TextButton(onClick = { viewModel.backFromSelectTeamStep() }) { Text("戻る") }
    }
}
