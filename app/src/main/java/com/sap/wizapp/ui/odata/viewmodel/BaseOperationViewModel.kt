package com.sap.wizapp.ui.odata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sap.wizapp.ui.odata.screens.OperationResult
import com.sap.wizapp.ui.odata.screens.OperationUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

open class BaseOperationViewModel(application: Application) : AndroidViewModel(application) {
    protected val _operationUiState =
        MutableStateFlow(OperationUIState())
    val operationUiState = _operationUiState.asStateFlow()

    fun resetOperationState() {
        _operationUiState.update { OperationUIState() }
    }

    fun operationFinished(result: OperationResult) {
        _operationUiState.update { OperationUIState(result = result) }
    }

    fun operationStart() {
        _operationUiState.update { it.copy(inProgress = true) }
    }

    private val _showSearchInput = MutableStateFlow(false)
    val showSearchInput: StateFlow<Boolean> = _showSearchInput
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun showSearchInput() {
        _showSearchInput.value = true
    }

    fun hideSearchInput() {
        _showSearchInput.value = false
    }

    fun onSearchQueryChanged(newText: String) {
        // Handle query text change
        _searchQuery.value = newText
    }
}
