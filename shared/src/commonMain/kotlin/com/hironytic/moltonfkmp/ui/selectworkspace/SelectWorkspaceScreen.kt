package com.hironytic.moltonfkmp.ui.selectworkspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SelectWorkspaceScreen(
    onCreateNewWorkspace: () -> Unit,
    onOpenWorkspace: (workspaceId: String) -> Unit,
    viewModel: SelectWorkspaceViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val workspacePendingDeletion by viewModel.workspacePendingDeletion.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.reload()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("観戦データ") }) },
    ) { innerPadding ->
        when (val state = uiState) {
            is SelectWorkspaceUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is SelectWorkspaceUiState.Loaded -> {
                if (state.workspaces.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("観戦データがありません")
                        Button(onClick = onCreateNewWorkspace) {
                            Text("観戦データを作成して始める")
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(state.workspaces, key = { it.id }) { workspace ->
                                ListItem(
                                    headlineContent = { Text(workspace.name) },
                                    trailingContent = {
                                        TextButton(onClick = { viewModel.requestDeletion(workspace) }) {
                                            Text("削除")
                                        }
                                    },
                                    modifier = Modifier.clickable { onOpenWorkspace(workspace.id) },
                                )
                            }
                        }
                        Button(
                            onClick = onCreateNewWorkspace,
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text("新しい観戦データで始める")
                        }
                    }
                }
            }
        }
    }

    val workspace = workspacePendingDeletion
    if (workspace != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeletion() },
            title = { Text("確認") },
            text = { Text("「${workspace.name}」を削除しますか?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeletion() }) { Text("削除") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeletion() }) { Text("キャンセル") }
            },
        )
    }
}
