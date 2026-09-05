package com.hironytic.moltonfkmp.ui.newworkspace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NewWorkspaceScreen(
    onExit: () -> Unit,
    onRegistered: (workspaceId: String) -> Unit,
    viewModel: NewWorkspaceViewModel = koinViewModel(),
) {
    val step by viewModel.step.collectAsState()
    val registeredWorkspaceId by viewModel.registeredWorkspaceId.collectAsState()

    LaunchedEffect(registeredWorkspaceId) {
        registeredWorkspaceId?.let { onRegistered(it) }
    }

    when (step) {
        NewWorkspaceStep.SELECT_STORY -> SelectStoryStep(viewModel, onExit)
        NewWorkspaceStep.SELECT_TEAM -> SelectTeamStep(viewModel)
        NewWorkspaceStep.SELECT_ROLE_OF_VILLAGER -> SelectRoleOfVillagerStep(viewModel)
        NewWorkspaceStep.SELECT_ROLE_OF_WOLF -> SelectRoleOfWolfStep(viewModel)
        NewWorkspaceStep.INPUT_NAME -> InputWorkspaceNameStep(viewModel)
        NewWorkspaceStep.CONFIRM -> ConfirmStep(viewModel)
    }
}
