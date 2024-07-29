package com.sap.wizapp.ui.odata.screens.product

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sap.wizapp.R
import com.sap.wizapp.ui.odata.*
import com.sap.wizapp.ui.odata.screens.*
import com.sap.wizapp.ui.odata.viewmodel.*
import com.sap.cloud.mobile.fiori.compose.text.model.FioriTextFieldContent
import com.sap.cloud.mobile.fiori.compose.text.ui.FioriSimpleTextField
import com.sap.wizapp.ui.odata.screens.OperationScreen
import com.sap.wizapp.ui.odata.screens.OperationScreenSettings
import com.sap.cloud.android.odata.espmcontainer.Product

val ProductEntityEditScreen: @Composable (
    navigateUp: (() -> Unit)?,
    viewModel: ODataViewModel,
    isExpandedScreen: Boolean
) -> Unit = { navigateUp, viewModel, isExpandedScreen ->
    val odataUIState by viewModel.odataUIState.collectAsState()
    val masterEntity = odataUIState.masterEntity!!
    val fieldStates = odataUIState.editorFiledStates
    val isCreation = odataUIState.entityOperationType == EntityOperationType.CREATE
    val isNavigateUp = remember {
        mutableStateOf(false)
    }

    if (isNavigateUp.value) {
        LeaveEditorWithConfirmation(navigateUp!!, isNavigateUp)
    }

    BackHandler(!isExpandedScreen) {
        isNavigateUp.value = true
    }

    val actions = listOf(
        ActionItem(
            nameRes = R.string.save,
            iconRes = R.drawable.ic_sap_icon_done,
            overflowMode = OverflowMode.IF_NECESSARY,
            enabled = fieldStates.none { it.isError },
            doAction = {
                viewModel.onSaveAction(
                    masterEntity,
                    fieldStates.map { Pair(it.property, it.value) })

            }),
    )

    OperationScreen(
        screenSettings = OperationScreenSettings(
            title = screenTitle(
                getEntityScreenInfo(viewModel.entityType, viewModel.entitySet),
                if (isCreation) ScreenType.Create else ScreenType.Update
            ),
            navigateUp = if (isExpandedScreen) null else ({ isNavigateUp.value = true }),
            actionItems = actions
        ),
        modifier = Modifier,
        viewModel = viewModel
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp)
        ) {
            itemsIndexed(odataUIState.editorFiledStates)
            { index, uiState ->
                FioriSimpleTextField(
                    value = uiState.value,
                    onValueChange = {
                        viewModel.updateFieldState(index, it)
                    },
                    content = FioriTextFieldContent(
                        label = uiState.property.name,
                        required = !uiState.property.isNullable,
                        errorMessage = uiState.errorMessage
                    ),
                    isError = uiState.isError,
                )
            }
        }
    }
}
