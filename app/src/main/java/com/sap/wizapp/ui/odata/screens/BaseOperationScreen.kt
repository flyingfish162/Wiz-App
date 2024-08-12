package com.sap.wizapp.ui.odata.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.sap.wizapp.ui.odata.ActionItem
import com.sap.wizapp.ui.odata.ActionMenu
import com.sap.wizapp.ui.AlertDialogComponent
import com.sap.wizapp.ui.odata.viewmodel.BaseOperationViewModel
import com.sap.cloud.mobile.fiori.compose.theme.fioriHorizonAttributes
import kotlinx.coroutines.launch

sealed class OperationResult {
    data class OperationSuccess(val message: String) : OperationResult()
    data class OperationFail(val message: String) : OperationResult()
}

data class OperationUIState(
    val inProgress: Boolean = false,
    val progress: Float? = null,
    val result: OperationResult? = null,
)

data class OperationScreenSettings(
    val title: String,
    val navigateUp: (() -> Unit)? = null,
    val actionItems: List<ActionItem> = listOf(),
    val floatingActionClick: (() -> Unit)? = null,
    val floatingActionIcon: ImageVector? = null
)

@Composable
fun floatingAction(
    floatingActionClick: (() -> Unit)? = null,
    floatingActionIcon: ImageVector? = null
): @Composable () -> Unit {
    return floatingActionClick?.let {
        {
            FloatingActionButton(onClick = floatingActionClick) {
                floatingActionIcon?.also { Icon(floatingActionIcon, "") }
            }
        }
    } ?: {}
}

@Composable
fun OperationScreen(
    modifier: Modifier = Modifier,
    screenSettings: OperationScreenSettings,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: BaseOperationViewModel,
    onFinishSuccess: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val operationUiState = viewModel.operationUiState.collectAsState()
    val launchScope = rememberCoroutineScope()

    val result = operationUiState.value.result
    result?.also {
        when (it) {
            is OperationResult.OperationFail -> {
                AlertDialogComponent(
                    onPositiveButtonClick = viewModel::resetOperationState,
                    text = it.message,
                )
            }
            is OperationResult.OperationSuccess -> {
                launchScope.launch {
                    snackbarHostState.showSnackbar(
                        message = it.message,
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.resetOperationState()
                onFinishSuccess()
            }
        }
    }

    Scaffold(
        topBar = {
            ODataAppBar(
                title = screenSettings.title,
                modifier = modifier,
                navigateUp = screenSettings.navigateUp,
                actionItems = screenSettings.actionItems,
                actionEnabled = !operationUiState.value.inProgress,
                showSearchInput = viewModel.showSearchInput.collectAsState().value,
                onSearchQueryChanged = viewModel::onSearchQueryChanged
            )
        },
        floatingActionButton = floatingAction(
            screenSettings.floatingActionClick,
            screenSettings.floatingActionIcon
        ),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            if (operationUiState.value.inProgress) {
                val progressModifier = Modifier.fillMaxWidth()
                operationUiState.value.progress?.also {
                    LinearProgressIndicator(
                        progress = { it },
                        modifier = progressModifier,
                        color = MaterialTheme.fioriHorizonAttributes.SapFioriColorHeaderCaption
                    )
                }
                    ?: LinearProgressIndicator(
                        progressModifier,
                        color = MaterialTheme.fioriHorizonAttributes.SapFioriColorHeaderCaption
                    )
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ODataAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigateUp: (() -> Unit)?,
    actionItems: List<ActionItem>,
    actionEnabled: Boolean = true,
    showSearchInput: Boolean,
    onSearchQueryChanged: (String) -> Unit
) {
    Column(modifier = Modifier) {
        TopAppBar(
            title = {
                if (showSearchInput) {
                    //import androidx.compose.runtime.getValue
                    //import androidx.compose.runtime.mutableStateOf
                    //import androidx.compose.runtime.setValue
                    var query by remember { mutableStateOf("") }
                    OutlinedTextField(
                        singleLine = true,
                        value = query,
                        onValueChange = {
                            query = it
                            onSearchQueryChanged(it)
                        },
                        label = { Text("Search") },
                        colors = OutlinedTextFieldDefaults.colors(
                            //import androidx.compose.ui.graphics.Color
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.Gray,
                            errorCursorColor = Color.Red
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(title)
                }
            },
            modifier = modifier,
            navigationIcon = {
                navigateUp?.also {
                    IconButton(onClick = it) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = ""
                        )
                    }
                }
            },
            actions = { ActionMenu(actionItems, isEnabled = actionEnabled) }
        )
    }

}
