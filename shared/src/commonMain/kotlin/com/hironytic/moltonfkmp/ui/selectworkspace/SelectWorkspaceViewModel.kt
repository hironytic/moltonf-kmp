package com.hironytic.moltonfkmp.ui.selectworkspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hironytic.moltonfkmp.storage.WorkspaceStore
import com.hironytic.moltonfkmp.workspace.Workspace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SelectWorkspaceUiState {
    data object Loading : SelectWorkspaceUiState
    data class Loaded(val workspaces: List<Workspace>) : SelectWorkspaceUiState
}

class SelectWorkspaceViewModel(
    private val workspaceStore: WorkspaceStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SelectWorkspaceUiState>(SelectWorkspaceUiState.Loading)
    val uiState: StateFlow<SelectWorkspaceUiState> = _uiState.asStateFlow()

    private val _workspacePendingDeletion = MutableStateFlow<Workspace?>(null)
    val workspacePendingDeletion: StateFlow<Workspace?> = _workspacePendingDeletion.asStateFlow()

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _uiState.value = SelectWorkspaceUiState.Loading
            _uiState.value = SelectWorkspaceUiState.Loaded(workspaceStore.getWorkspaces())
        }
    }

    fun requestDeletion(workspace: Workspace) {
        _workspacePendingDeletion.value = workspace
    }

    fun cancelDeletion() {
        _workspacePendingDeletion.value = null
    }

    fun confirmDeletion() {
        val workspace = _workspacePendingDeletion.value ?: return
        _workspacePendingDeletion.value = null
        viewModelScope.launch {
            workspaceStore.remove(workspace)
            reload()
        }
    }
}
