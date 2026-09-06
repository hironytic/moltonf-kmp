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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.story.parseStory
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.launch

@Composable
fun SelectStoryStep(viewModel: NewWorkspaceViewModel, onExit: () -> Unit) {
    val storyName by viewModel.storyName.collectAsState()
    val archiveLoadError by viewModel.archiveLoadError.collectAsState()
    val scope = rememberCoroutineScope()

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("xml")),
    ) { file ->
        if (file != null) {
            scope.launch {
                try {
                    val story = parseStory(file.readString())
                    viewModel.onArchiveLoaded(story)
                } catch (e: Exception) {
                    viewModel.onArchiveLoadFailed()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("村データの読み込み", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Jindolf XmlScheme (https://github.com/olyutorskii/XmlScheme) 形式のXMLファイルを用意してください。" +
                "そのXMLファイルの村データを読み込みます。" +
                "読み込んだデータは端末内のストレージに保存されます" +
                "（観戦データが作成された後は、ここで選択したXMLファイルはもう参照しません）。",
        )

        if (storyName != null) {
            Text("読み込み済みの村: $storyName", fontWeight = FontWeight.Bold)
        }

        Button(onClick = { launcher.launch() }) {
            Text(if (storyName != null) "別のファイルを選択する" else "村データを読み込む")
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onExit) { Text("戻る") }
            if (storyName != null) {
                Button(onClick = { viewModel.forwardFromSelectStoryStep() }) { Text("次へ") }
            }
        }
    }

    if (archiveLoadError) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissArchiveLoadError() },
            title = { Text("村データの読み込み") },
            text = { Text("指定されたファイルから、村データを読み込めませんでした。") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissArchiveLoadError() }) { Text("OK") }
            },
        )
    }
}
