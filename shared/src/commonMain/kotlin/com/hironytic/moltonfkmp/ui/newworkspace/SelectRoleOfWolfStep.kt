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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.ui.theme.NeutralOutlinedButton
import com.hironytic.moltonfkmp.ui.theme.ScreenTitle

@Composable
fun SelectRoleOfWolfStep(viewModel: NewWorkspaceViewModel) {
    val options by viewModel.wolfRoleOptions.collectAsState()
    val selected by viewModel.wolfRole.collectAsState()
    val canForward by viewModel.canForwardFromSelectRoleOfWolfStep.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("人狼側の役職は？")
        Text("人狼側の役職を選んでください。人狼側の勝利条件は残る村人の数が人狼と同数以下になることです。")

        OptionChooser(
            options = options,
            selected = selected,
            onChoose = { viewModel.selectWolfRole(it) },
        ) { option ->
            when (option) {
                WolfRoleOption.WOLF -> OptionContent("人狼", "正体を悟られないようにしつつ村人を襲撃する視点で観戦します。")
                WolfRoleOption.MADMAN -> OptionContent("狂人", "人間ながら人狼の繁栄を望む視点で観戦します。")
                WolfRoleOption.LONGEST_SURVIVOR -> OptionContent("長く生き残った人狼", "最も長く潜んだ人狼の視点で観戦します（狂人にはなりません）。")
                WolfRoleOption.ANYTHING -> OptionContent("人狼側の中からおまかせ", "観戦データ作成時にシステムがランダムに決定します。")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeutralOutlinedButton(onClick = { viewModel.backFromSelectRoleOfWolfStep() }, text = "戻る")
            Button(onClick = { viewModel.forwardFromSelectRoleOfWolfStep() }, enabled = canForward) { Text("次へ") }
        }
    }
}
