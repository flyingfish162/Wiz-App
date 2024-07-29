package com.sap.wizapp.ui.odata

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sap.wizapp.R
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata.EntityTypes
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata.EntitySets
import com.sap.wizapp.ui.odata.screens.customer.CustomerEntitiesScreen
import com.sap.wizapp.ui.odata.screens.customer.CustomerEntityEditScreen
import com.sap.wizapp.ui.odata.screens.customer.CustomerEntityDetailScreen
import com.sap.wizapp.ui.odata.screens.customer.CustomerEntitiesExpandScreen
import com.sap.wizapp.ui.odata.screens.productcategory.ProductCategoryEntitiesScreen
import com.sap.wizapp.ui.odata.screens.productcategory.ProductCategoryEntityEditScreen
import com.sap.wizapp.ui.odata.screens.productcategory.ProductCategoryEntityDetailScreen
import com.sap.wizapp.ui.odata.screens.productcategory.ProductCategoryEntitiesExpandScreen
import com.sap.wizapp.ui.odata.screens.producttext.ProductTextEntitiesScreen
import com.sap.wizapp.ui.odata.screens.producttext.ProductTextEntityEditScreen
import com.sap.wizapp.ui.odata.screens.producttext.ProductTextEntityDetailScreen
import com.sap.wizapp.ui.odata.screens.producttext.ProductTextEntitiesExpandScreen
import com.sap.wizapp.ui.odata.screens.product.ProductEntitiesScreen
import com.sap.wizapp.ui.odata.screens.product.ProductEntityEditScreen
import com.sap.wizapp.ui.odata.screens.product.ProductEntityDetailScreen
import com.sap.wizapp.ui.odata.screens.product.ProductEntitiesExpandScreen
import com.sap.wizapp.ui.odata.screens.purchaseorderheader.PurchaseOrderHeaderEntitiesScreen
import com.sap.wizapp.ui.odata.screens.purchaseorderheader.PurchaseOrderHeaderEntityEditScreen
import com.sap.wizapp.ui.odata.screens.purchaseorderheader.PurchaseOrderHeaderEntityDetailScreen
import com.sap.wizapp.ui.odata.screens.purchaseorderheader.PurchaseOrderHeaderEntitiesExpandScreen
import com.sap.wizapp.ui.odata.screens.purchaseorderitem.PurchaseOrderItemEntitiesScreen
import com.sap.wizapp.ui.odata.screens.purchaseorderitem.PurchaseOrderItemEntityEditScreen
import com.sap.wizapp.ui.odata.screens.purchaseorderitem.PurchaseOrderItemEntityDetailScreen
import com.sap.wizapp.ui.odata.screens.purchaseorderitem.PurchaseOrderItemEntitiesExpandScreen
import com.sap.wizapp.ui.odata.screens.salesorderheader.SalesOrderHeaderEntitiesScreen
import com.sap.wizapp.ui.odata.screens.salesorderheader.SalesOrderHeaderEntityEditScreen
import com.sap.wizapp.ui.odata.screens.salesorderheader.SalesOrderHeaderEntityDetailScreen
import com.sap.wizapp.ui.odata.screens.salesorderheader.SalesOrderHeaderEntitiesExpandScreen
import com.sap.wizapp.ui.odata.screens.salesorderitem.SalesOrderItemEntitiesScreen
import com.sap.wizapp.ui.odata.screens.salesorderitem.SalesOrderItemEntityEditScreen
import com.sap.wizapp.ui.odata.screens.salesorderitem.SalesOrderItemEntityDetailScreen
import com.sap.wizapp.ui.odata.screens.salesorderitem.SalesOrderItemEntitiesExpandScreen
import com.sap.wizapp.ui.odata.screens.stock.StockEntitiesScreen
import com.sap.wizapp.ui.odata.screens.stock.StockEntityEditScreen
import com.sap.wizapp.ui.odata.screens.stock.StockEntityDetailScreen
import com.sap.wizapp.ui.odata.screens.stock.StockEntitiesExpandScreen
import com.sap.wizapp.ui.odata.screens.supplier.SupplierEntitiesScreen
import com.sap.wizapp.ui.odata.screens.supplier.SupplierEntityEditScreen
import com.sap.wizapp.ui.odata.screens.supplier.SupplierEntityDetailScreen
import com.sap.wizapp.ui.odata.screens.supplier.SupplierEntitiesExpandScreen
import com.sap.wizapp.ui.odata.viewmodel.ODataViewModel
import com.sap.cloud.mobile.kotlin.odata.EntitySet
import com.sap.cloud.mobile.kotlin.odata.EntityType
import com.sap.cloud.mobile.kotlin.odata.EntityValue
import com.sap.cloud.mobile.kotlin.odata.NavigationProperty

/*
enum class EntityTypeScreenInfo(
    val entityType: EntityType, val setTitleId: Int, val itemTitleId: Int, val iconId: Int
) {
    Customers(
        EntityTypes.customer,
        R.string.eset_customers,
        R.string.eset_customers_single,
        R.drawable.ic_sap_icon_product_filled_round
        ),
    ProductCategories(
        EntityTypes.productCategory,
        R.string.eset_productcategories,
        R.string.eset_productcategories_single,
        R.drawable.ic_sap_icon_product_outlined
        ),
    ProductTexts(
        EntityTypes.productText,
        R.string.eset_producttexts,
        R.string.eset_producttexts_single,
        R.drawable.ic_sap_icon_product_filled_round
        ),
    Products(
        EntityTypes.product,
        R.string.eset_products,
        R.string.eset_products_single,
        R.drawable.ic_sap_icon_product_outlined
        ),
    PurchaseOrderHeaders(
        EntityTypes.purchaseOrderHeader,
        R.string.eset_purchaseorderheaders,
        R.string.eset_purchaseorderheaders_single,
        R.drawable.ic_sap_icon_product_filled_round
        ),
    PurchaseOrderItems(
        EntityTypes.purchaseOrderItem,
        R.string.eset_purchaseorderitems,
        R.string.eset_purchaseorderitems_single,
        R.drawable.ic_sap_icon_product_outlined
        ),
    SalesOrderHeaders(
        EntityTypes.salesOrderHeader,
        R.string.eset_salesorderheaders,
        R.string.eset_salesorderheaders_single,
        R.drawable.ic_sap_icon_product_filled_round
        ),
    SalesOrderItems(
        EntityTypes.salesOrderItem,
        R.string.eset_salesorderitems,
        R.string.eset_salesorderitems_single,
        R.drawable.ic_sap_icon_product_outlined
        ),
    Stock(
        EntityTypes.stock,
        R.string.eset_stock,
        R.string.eset_stock_single,
        R.drawable.ic_sap_icon_product_filled_round
        ),
    Suppliers(
        EntityTypes.supplier,
        R.string.eset_suppliers,
        R.string.eset_suppliers_single,
        R.drawable.ic_sap_icon_product_outlined
        )
}*/

enum class EntityScreenInfo(
    val entityType: EntityType,
    val entitySet: EntitySet?,
    val setTitleId: Int, val itemTitleId: Int, val iconId: Int,
    val entityExpandScreen: @Composable (
        navigateToHome: () -> Unit,
        navigateUp: () -> Unit,
        onNavigateProperty: (EntityValue, NavigationProperty) -> Unit,
        viewModel: ODataViewModel,
    ) -> Unit,
    val entityListScreen: @Composable (
        navigateToHome: () -> Unit,
        navigateUp: () -> Unit,
        viewModel: ODataViewModel,
        isExpandedScreen: Boolean
    ) -> Unit,
    val entityEditScreen: @Composable (
        navigateUp: () -> Unit, viewModel: ODataViewModel, isExpandedScreen: Boolean
    ) -> Unit,
    val entityDetailScreen: @Composable (
        onNavigateProperty: (EntityValue, NavigationProperty) -> Unit, navigateUp: () -> Unit, viewModel: ODataViewModel, isExpandedScreen: Boolean
    ) -> Unit,
) {
    Customers(
        EntityTypes.customer,
        EntitySets.customers,
        R.string.eset_customers,
        R.string.eset_customers_single,
        R.drawable.ic_sap_icon_product_filled_round,
        CustomerEntitiesExpandScreen,
        CustomerEntitiesScreen,
        CustomerEntityEditScreen,
        CustomerEntityDetailScreen
    ),
    ProductCategories(
        EntityTypes.productCategory,
        EntitySets.productCategories,
        R.string.eset_productcategories,
        R.string.eset_productcategories_single,
        R.drawable.ic_sap_icon_product_outlined,
        ProductCategoryEntitiesExpandScreen,
        ProductCategoryEntitiesScreen,
        ProductCategoryEntityEditScreen,
        ProductCategoryEntityDetailScreen
    ),
    ProductTexts(
        EntityTypes.productText,
        EntitySets.productTexts,
        R.string.eset_producttexts,
        R.string.eset_producttexts_single,
        R.drawable.ic_sap_icon_product_filled_round,
        ProductTextEntitiesExpandScreen,
        ProductTextEntitiesScreen,
        ProductTextEntityEditScreen,
        ProductTextEntityDetailScreen
    ),
    Products(
        EntityTypes.product,
        EntitySets.products,
        R.string.eset_products,
        R.string.eset_products_single,
        R.drawable.ic_sap_icon_product_outlined,
        ProductEntitiesExpandScreen,
        ProductEntitiesScreen,
        ProductEntityEditScreen,
        ProductEntityDetailScreen
    ),
    PurchaseOrderHeaders(
        EntityTypes.purchaseOrderHeader,
        EntitySets.purchaseOrderHeaders,
        R.string.eset_purchaseorderheaders,
        R.string.eset_purchaseorderheaders_single,
        R.drawable.ic_sap_icon_product_filled_round,
        PurchaseOrderHeaderEntitiesExpandScreen,
        PurchaseOrderHeaderEntitiesScreen,
        PurchaseOrderHeaderEntityEditScreen,
        PurchaseOrderHeaderEntityDetailScreen
    ),
    PurchaseOrderItems(
        EntityTypes.purchaseOrderItem,
        EntitySets.purchaseOrderItems,
        R.string.eset_purchaseorderitems,
        R.string.eset_purchaseorderitems_single,
        R.drawable.ic_sap_icon_product_outlined,
        PurchaseOrderItemEntitiesExpandScreen,
        PurchaseOrderItemEntitiesScreen,
        PurchaseOrderItemEntityEditScreen,
        PurchaseOrderItemEntityDetailScreen
    ),
    SalesOrderHeaders(
        EntityTypes.salesOrderHeader,
        EntitySets.salesOrderHeaders,
        R.string.eset_salesorderheaders,
        R.string.eset_salesorderheaders_single,
        R.drawable.ic_sap_icon_product_filled_round,
        SalesOrderHeaderEntitiesExpandScreen,
        SalesOrderHeaderEntitiesScreen,
        SalesOrderHeaderEntityEditScreen,
        SalesOrderHeaderEntityDetailScreen
    ),
    SalesOrderItems(
        EntityTypes.salesOrderItem,
        EntitySets.salesOrderItems,
        R.string.eset_salesorderitems,
        R.string.eset_salesorderitems_single,
        R.drawable.ic_sap_icon_product_outlined,
        SalesOrderItemEntitiesExpandScreen,
        SalesOrderItemEntitiesScreen,
        SalesOrderItemEntityEditScreen,
        SalesOrderItemEntityDetailScreen
    ),
    Stock(
        EntityTypes.stock,
        EntitySets.stock,
        R.string.eset_stock,
        R.string.eset_stock_single,
        R.drawable.ic_sap_icon_product_filled_round,
        StockEntitiesExpandScreen,
        StockEntitiesScreen,
        StockEntityEditScreen,
        StockEntityDetailScreen
    ),
    Suppliers(
        EntityTypes.supplier,
        EntitySets.suppliers,
        R.string.eset_suppliers,
        R.string.eset_suppliers_single,
        R.drawable.ic_sap_icon_product_outlined,
        SupplierEntitiesExpandScreen,
        SupplierEntitiesScreen,
        SupplierEntityEditScreen,
        SupplierEntityDetailScreen
    ),
}

fun getEntitySetScreenInfoList(): List<EntityScreenInfo> {
    val metadataMap = EntityMetaData.entries.associateBy { it.entityType }
    return EntityScreenInfo.entries.filter { metadataMap[it.entityType]?.entitySet != null }
}

// return screen info according to specified entity type and entity set
fun getEntityScreenInfo(entityType: EntityType, entitySet: EntitySet?): EntityScreenInfo =
    EntityScreenInfo.entries.first { getKey(entityType, entitySet) == getKey(it.entityType, it.entitySet) }

enum class ScreenType {
    List, Details, Update, Create, NavigatedList
}

@Composable
fun screenTitle(entitySetScreenInfo: EntityScreenInfo, screenType: ScreenType): String {
    return when (screenType) {
        //TODO: navigated list title?
        ScreenType.List, ScreenType.NavigatedList -> stringResource(id = entitySetScreenInfo.setTitleId)
        ScreenType.Details -> stringResource(id = entitySetScreenInfo.itemTitleId)
        ScreenType.Update -> stringResource(id = R.string.title_update_fragment) + " ${
            stringResource(
                id = entitySetScreenInfo.itemTitleId
            )
        }"
        ScreenType.Create -> stringResource(
            id = R.string.title_create_fragment,
            stringResource(id = entitySetScreenInfo.itemTitleId)
        )
    }
}
