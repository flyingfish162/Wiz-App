package com.sap.wizapp.ui.odata.screens.product

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sap.wizapp.R
import com.sap.wizapp.ui.odata.*
import com.sap.wizapp.ui.odata.screens.*
import com.sap.wizapp.service.SAPServiceManager
import com.sap.wizapp.ui.odata.data.EntityMediaResource
import com.sap.wizapp.ui.odata.viewmodel.ODataViewModel
import com.sap.cloud.mobile.fiori.compose.keyvaluecell.model.FioriKeyValueCellContent
import com.sap.cloud.mobile.fiori.compose.keyvaluecell.ui.FioriKeyValueCell
import com.sap.cloud.mobile.fiori.compose.objectheader.model.*
import com.sap.cloud.mobile.fiori.compose.objectheader.ui.FioriObjectHeader
import com.sap.cloud.mobile.kotlin.odata.EntityValue
import com.sap.cloud.mobile.kotlin.odata.NavigationProperty
import com.sap.wizapp.ui.odata.screens.OperationScreen
import com.sap.wizapp.ui.odata.screens.OperationScreenSettings
import com.sap.wizapp.ui.odata.screens.defaultObjectHeaderData
import com.sap.cloud.mobile.fiori.compose.theme.fioriHorizonAttributes
import com.sap.cloud.android.odata.espmcontainer.Product

val ProductEntityDetailScreen: @Composable (
    onNavigateProperty: (EntityValue, NavigationProperty) -> Unit,
    navigateUp: (() -> Unit)?,
    viewModel: ODataViewModel,
    isExpandedScreen: Boolean
) -> Unit = { onNavigateProperty, navigateUp, viewModel, isExpandedScreen ->
    val uiState by viewModel.odataUIState.collectAsState()

    val deleteConfirm = remember {
        mutableStateOf(false)
    }

    DeleteEntityWithConfirmation(viewModel, deleteConfirm)

    OperationScreen(
        screenSettings = OperationScreenSettings(
            title = screenTitle(getEntityScreenInfo(viewModel.entityType, viewModel.entitySet), ScreenType.Details),
            actionItems = listOf(
                ActionItem(
                    nameRes = R.string.menu_update,
                    iconRes = R.drawable.ic_sap_icon_edit,
                    overflowMode = OverflowMode.IF_NECESSARY,
                    doAction = viewModel::onEditAction
                ),
                ActionItem(
                    nameRes = R.string.menu_delete,
                    iconRes = R.drawable.ic_sap_icon_delete,
                    overflowMode = OverflowMode.IF_NECESSARY,
                    doAction = { deleteConfirm.value = true }
                ),
            ),
            navigateUp = if (isExpandedScreen) null else navigateUp,
        ),
        modifier = Modifier,
        viewModel = viewModel
    ) {
        Column {
            val entity = uiState.masterEntity
            if (entity != null) {
                FioriObjectHeader(
                    modifier = Modifier.fillMaxWidth(),
                    primaryPage = defaultObjectHeaderData(
                        title = viewModel.getEntityTitle(entity),
                        imageUrl = EntityMediaResource.getMediaResourceUrl(
                            entity,
                            SAPServiceManager.serviceRoot
                        ),
                        imageChars = viewModel.getAvatarText(entity)
                    ),
                    statusLayout = FioriObjectHeaderStatusLayout.Inline
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.productID.name,
                                value = entity.getOptionalValue(Product.productID)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.category.name,
                                value = entity.getOptionalValue(Product.category)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.categoryName.name,
                                value = entity.getOptionalValue(Product.categoryName)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.currencyCode.name,
                                value = entity.getOptionalValue(Product.currencyCode)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.dimensionDepth.name,
                                value = entity.getOptionalValue(Product.dimensionDepth)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.dimensionHeight.name,
                                value = entity.getOptionalValue(Product.dimensionHeight)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.dimensionUnit.name,
                                value = entity.getOptionalValue(Product.dimensionUnit)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.dimensionWidth.name,
                                value = entity.getOptionalValue(Product.dimensionWidth)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.longDescription.name,
                                value = entity.getOptionalValue(Product.longDescription)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.name.name,
                                value = entity.getOptionalValue(Product.name)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.pictureUrl.name,
                                value = entity.getOptionalValue(Product.pictureUrl)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.price.name,
                                value = entity.getOptionalValue(Product.price)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.quantityUnit.name,
                                value = entity.getOptionalValue(Product.quantityUnit)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.shortDescription.name,
                                value = entity.getOptionalValue(Product.shortDescription)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.supplierID.name,
                                value = entity.getOptionalValue(Product.supplierID)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.weight.name,
                                value = entity.getOptionalValue(Product.weight)?.toString() ?: ""
                            )
                        )
                    FioriKeyValueCell(
                        content = FioriKeyValueCellContent(
                                key = Product.weightUnit.name,
                                value = entity.getOptionalValue(Product.weightUnit)?.toString() ?: ""
                            )
                        )
                    TextButton(onClick = {
                        onNavigateProperty(entity, Product.supplier as NavigationProperty)
                    }) {
                        Text("Supplier", color = MaterialTheme.fioriHorizonAttributes.SapFioriColorT6)
                    }

                    TextButton(onClick = {
                        onNavigateProperty(entity, Product.stock as NavigationProperty)
                    }) {
                        Text("Stock", color = MaterialTheme.fioriHorizonAttributes.SapFioriColorT6)
                    }

                    TextButton(onClick = {
                        onNavigateProperty(entity, Product.purchaseOrderItems as NavigationProperty)
                    }) {
                        Text("PurchaseOrderItems", color = MaterialTheme.fioriHorizonAttributes.SapFioriColorT6)
                    }

                    TextButton(onClick = {
                        onNavigateProperty(entity, Product.salesOrderItems as NavigationProperty)
                    }) {
                        Text("SalesOrderItems", color = MaterialTheme.fioriHorizonAttributes.SapFioriColorT6)
                    }

                }
            }
        }
    }
}

