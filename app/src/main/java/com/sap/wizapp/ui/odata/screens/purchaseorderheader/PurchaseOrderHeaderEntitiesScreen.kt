package com.sap.wizapp.ui.odata.screens.purchaseorderheader

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.sap.wizapp.R
import com.sap.wizapp.service.SAPServiceManager
import com.sap.wizapp.ui.odata.*
import com.sap.wizapp.ui.odata.screens.*
import com.sap.wizapp.ui.odata.data.EntityMediaResource
import com.sap.wizapp.ui.odata.viewmodel.*
import com.sap.cloud.mobile.fiori.compose.avatar.model.*
import com.sap.cloud.mobile.fiori.compose.common.FioriIcon
import com.sap.cloud.mobile.fiori.compose.common.FioriImage
import com.sap.cloud.mobile.fiori.compose.objectcell.model.*
import com.sap.cloud.mobile.fiori.compose.objectcell.ui.FioriObjectCell
import com.sap.cloud.mobile.fiori.compose.objectcell.ui.FioriObjectCellDefaults
import com.sap.cloud.mobile.fiori.compose.theme.fioriHorizonAttributes
import com.sap.cloud.mobile.kotlin.odata.EntityValue
import com.sap.cloud.mobile.onboarding.compose.screens.LoadingItem
import com.sap.wizapp.ui.AlertDialogComponent
import com.sap.wizapp.ui.odata.screens.*

//TODO: pull down screen to refresh
//https://github.com/aakarshrestha/compose-swipe-to-refresh
//https://google.github.io/accompanist/swiperefresh/
val PurchaseOrderHeaderEntitiesScreen:
    @Composable
        (
        navigateToHome: () -> Unit,
        navigateUp: () -> Unit,
        viewModel: ODataViewModel,
        isExpandScreen: Boolean,
    ) -> Unit =
{ navigateToHome, navigateUp, viewModel, isExpandedScreen ->
    val entities = viewModel.pagingDataState.value.collectAsLazyPagingItems()
    val uiState by viewModel.odataUIState.collectAsState()

    val listState: LazyListState = rememberLazyListState()

    val isInCreateOrUpdate = remember {
        derivedStateOf {
            uiState.entityOperationType in setOf(
            EntityOperationType.CREATE,
            EntityUpdateOperationType.UPDATE_FROM_LIST,
            EntityUpdateOperationType.UPDATE_FROM_DETAIL
        ) }
    }

    var onLeave by remember {
        mutableStateOf(navigateUp)
    }

    val deleteConfirm = remember {
        mutableStateOf(false)
    }

    DeleteEntityWithConfirmation(viewModel, deleteConfirm)

    val leaveConfirm = remember {
        mutableStateOf(false)
    }

    val onNavigateUp = remember {
        {
            leaveConfirm.value = true
            onLeave = navigateUp
        }
    }

    val onClickChange: (EntityValue) -> Unit = remember {
        { entity ->
            if (isExpandedScreen && entity != uiState.masterEntity && isInCreateOrUpdate.value) {
                leaveConfirm.value = true
                onLeave = { viewModel.onClickAction(entity) }
            } else viewModel.onClickAction(entity)
        }
    }

    LeaveEditorWithConfirmation(onLeave, leaveConfirm)

    val actionItems =
        if (isExpandedScreen) listOf()
        else getSelectedItemActionsList(
            navigateToHome,
            viewModel,
            deleteConfirm
        )

    OperationScreen(
        screenSettings = OperationScreenSettings(
            title = screenTitle(getEntityScreenInfo(viewModel.entityType, viewModel.entitySet), ScreenType.List),
            navigateUp = if (isInCreateOrUpdate.value) onNavigateUp else navigateUp,
            actionItems = actionItems,
            floatingActionClick = viewModel.onFloatingAdd(),
            floatingActionIcon = Icons.Filled.Add
        ),
        modifier = Modifier,
        viewModel = viewModel
    ) {
        if (entities.loadState.refresh == LoadState.Loading) {
            LoadingItem()
        } else {
            LazyColumn(state = listState) {
                items(
                    count = entities.itemCount,
                ) { index ->
                    val entity = entities[index] ?: return@items
                    val selected = uiState.selectedItems.contains(entity)
                    val avatar = FioriAvatarConstruct(
                        hasBadge = false,
                        type = FioriAvatarType.SINGLE,
                        avatarList = listOf(
                            if (!selected) {
                                if (EntityMediaResource.hasMediaResources(entity.entityType)) {
                                        FioriAvatarData(
                                            FioriImage(
                                                url = EntityMediaResource.getMediaResourceUrl(
                                                    entity,
                                                    SAPServiceManager.serviceRoot
                                                )!!
                                            ),
                                            shape = FioriAvatarShape.ROUNDEDCORNER
                                        )
                                } else FioriAvatarData(
                                    text = viewModel.getAvatarText(entity).uppercase(),
                                    textColor = MaterialTheme.fioriHorizonAttributes.SapFioriColorBaseText
                                )
                            } else FioriAvatarData(
                                FioriImage(resId = R.drawable.ic_sap_icon_done),
                                color = MaterialTheme.fioriHorizonAttributes.SapFioriColorHeaderCaption,
                                size = 40.dp,
                            )
                        ),
                        size = 40.dp,
                        shape = FioriAvatarShape.CIRCLE,
//                      backgroundColor = MaterialTheme.fioriHorizonAttributes.SapFioriColorS6
                    )
                    val stateIcon = getEntityStateIcon(entity)
                    val objectCellData = FioriObjectCellData(
                        headline = viewModel.getEntityTitle(entity),
                        iconStack = listOf(
                            IconStackElement(viewModel.getAvatarText(entity).uppercase()),
                            IconStackElement(
                                FioriIcon(
                                    resId = com.sap.cloud.mobile.fiori.compose.R.drawable.avatar_badge,
                                    contentDescription = stringResource(id = stateIcon.desc),
                                    tint = MaterialTheme.fioriHorizonAttributes.SapFioriColorSectionDivider
                                )
                            )
                        ),
                        subheadline = "Subtitle goes here",
                        footnote = "caption display",
                        avatar = avatar,
                    ).apply { setDisplayReadIndicator(false) }

                    FioriObjectCell(
                        cellData = objectCellData,
                        colors = FioriObjectCellDefaults.colors(),
                        textStyles = FioriObjectCellDefaults.textStyles(),
                        styles = FioriObjectCellDefaults.styles(iconStackSize = 10.dp),
                        onClick = { onClickChange(entity) },
                        onLongPress = { viewModel.onSelectAction(entity) }
                    )
                }
            }
        }
    }
}
