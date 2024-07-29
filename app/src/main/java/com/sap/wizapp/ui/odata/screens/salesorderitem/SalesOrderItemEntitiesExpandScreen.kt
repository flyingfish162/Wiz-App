package com.sap.wizapp.ui.odata.screens.salesorderitem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.sap.wizapp.ui.odata.ScreenType
import com.sap.wizapp.ui.odata.getEntityScreenInfo
import com.sap.wizapp.ui.odata.screenTitle
import com.sap.wizapp.ui.odata.screens.DeleteEntityWithConfirmation
import com.sap.wizapp.ui.odata.screens.OperationScreen
import com.sap.wizapp.ui.odata.screens.OperationScreenSettings
import com.sap.wizapp.ui.odata.screens.getSelectedItemActionsList
import com.sap.wizapp.ui.odata.viewmodel.EntityOperationType
import com.sap.wizapp.ui.odata.viewmodel.EntityUpdateOperationType
import com.sap.wizapp.ui.odata.viewmodel.ODataViewModel
import com.sap.cloud.mobile.kotlin.odata.EntityValue
import com.sap.cloud.mobile.kotlin.odata.NavigationProperty

val SalesOrderItemEntitiesExpandScreen:
        @Composable (
            navigateToHome: () -> Unit,
            navigateUp: () -> Unit,
            onNavigateProperty: (EntityValue, NavigationProperty) -> Unit,
            viewModel: ODataViewModel,
        ) -> Unit =
    { navigateToHome, navigateUp, onNavigateProperty, viewModel ->
        val uiState by viewModel.odataUIState.collectAsState()
        Row(modifier = Modifier) {
            Box(modifier = Modifier.weight(1f)) {
                SalesOrderItemEntitiesScreen(
                    navigateToHome,
                    navigateUp,
                    viewModel,
                    true
                )
            }
            Box(modifier = Modifier.weight(2f)) {
                when (uiState.entityOperationType) {
                    EntityOperationType.DETAIL -> {
                        SalesOrderItemEntityDetailScreen(onNavigateProperty, null, viewModel, true)
                    }

                    EntityOperationType.CREATE, EntityUpdateOperationType.UPDATE_FROM_LIST, EntityUpdateOperationType.UPDATE_FROM_DETAIL -> {
                        SalesOrderItemEntityEditScreen(null, viewModel, true)
                    }

                    else -> {
                        SalesOrderItemBlankScreen(viewModel)
                    }
                }
            }
        }
    }

val SalesOrderItemBlankScreen:
        @Composable (
            viewModel: ODataViewModel,
        ) -> Unit =
    { viewModel ->
    val deleteState = remember {
        mutableStateOf(false)
    }
    OperationScreen(
        screenSettings = OperationScreenSettings(
            title = screenTitle(
                getEntityScreenInfo(viewModel.entityType, viewModel.entitySet),
                ScreenType.Details
            ),
            actionItems = getSelectedItemActionsList(
                viewModel,
                deleteState
            ),
            navigateUp = null,
        ),
        modifier = Modifier,
        viewModel = viewModel
    ) {
        Box(modifier = Modifier)
        DeleteEntityWithConfirmation(viewModel, deleteState)
    }
}
