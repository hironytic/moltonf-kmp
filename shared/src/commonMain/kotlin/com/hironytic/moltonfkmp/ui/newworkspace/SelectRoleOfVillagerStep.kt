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
fun SelectRoleOfVillagerStep(viewModel: NewWorkspaceViewModel) {
    val options by viewModel.villagerRoleOptions.collectAsState()
    val selected by viewModel.villagerRole.collectAsState()
    val canForward by viewModel.canForwardFromSelectRoleOfVillagerStep.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("村人側の役職は？")
        Text("村人側の役職を選んでください。村人側の勝利条件は全ての人狼を処刑することです。")

        OptionChooser(
            options = options,
            selected = selected,
            onChoose = { viewModel.selectVillagerRole(it) },
        ) { option ->
            when (option) {
                VillagerRoleOption.INNOCENT -> OptionContent("ただの村人", "特別な能力を持たない村人の視点で観戦します。")
                VillagerRoleOption.SEER -> OptionContent("占い師", "毎夜、誰かが人狼かどうかを占える視点で観戦します。")
                VillagerRoleOption.SHAMAN -> OptionContent("霊能者", "処刑された者が人狼かどうかを知ることのできる視点で観戦します。")
                VillagerRoleOption.HUNTER -> OptionContent("狩人", "誰かを人狼の襲撃から守れる視点で観戦します。")
                VillagerRoleOption.FRATER -> OptionContent("共有者", "もうひとりの共有者が誰かを知ることができる視点で観戦します。")
                VillagerRoleOption.LONGEST_SURVIVOR -> OptionContent("長く生き残った人", "村人側の人間のうち、最も長く生き残った人の視点で観戦します。")
                VillagerRoleOption.ANYTHING -> OptionContent("村人側の中からおまかせ", "観戦データ作成時にシステムがランダムに決定します。")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeutralOutlinedButton(onClick = { viewModel.backFromSelectRoleOfVillagerStep() }, text = "戻る")
            Button(onClick = { viewModel.forwardFromSelectRoleOfVillagerStep() }, enabled = canForward) { Text("次へ") }
        }
    }
}
