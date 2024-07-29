package com.sap.wizapp.ui.odata

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sap.wizapp.ui.odata.viewmodel.ODataViewModel
import com.sap.wizapp.ui.odata.viewmodel.customer.CustomerODataViewModel
import com.sap.wizapp.ui.odata.viewmodel.productcategory.ProductCategoryODataViewModel
import com.sap.wizapp.ui.odata.viewmodel.producttext.ProductTextODataViewModel
import com.sap.wizapp.ui.odata.viewmodel.product.ProductODataViewModel
import com.sap.wizapp.ui.odata.viewmodel.purchaseorderheader.PurchaseOrderHeaderODataViewModel
import com.sap.wizapp.ui.odata.viewmodel.purchaseorderitem.PurchaseOrderItemODataViewModel
import com.sap.wizapp.ui.odata.viewmodel.salesorderheader.SalesOrderHeaderODataViewModel
import com.sap.wizapp.ui.odata.viewmodel.salesorderitem.SalesOrderItemODataViewModel
import com.sap.wizapp.ui.odata.viewmodel.stock.StockODataViewModel
import com.sap.wizapp.ui.odata.viewmodel.supplier.SupplierODataViewModel
import com.sap.cloud.mobile.kotlin.odata.EntitySet
import com.sap.cloud.mobile.kotlin.odata.EntityType
import com.sap.cloud.mobile.kotlin.odata.EntityValue
import com.sap.cloud.mobile.kotlin.odata.Property
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata.EntitySets
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata.EntityTypes

class ODataEntityViewModelFactory(
    private val application: Application,
    private val entityType: EntityType,
    private val entitySet: EntitySet?,
    private val orderByProperty: Property?,
    private val parent: EntityValue? = null,
    private val navigationPropertyName: String? = null,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when (getKey(entityType, entitySet)) {
            getKey(EntityTypes.customer, EntitySets.customers) -> CustomerODataViewModel(
                application,
                orderByProperty,
                parent,
                navigationPropertyName
            ) as T
            getKey(EntityTypes.productCategory, EntitySets.productCategories) -> ProductCategoryODataViewModel(
                application,
                orderByProperty,
                parent,
                navigationPropertyName
            ) as T
            getKey(EntityTypes.productText, EntitySets.productTexts) -> ProductTextODataViewModel(
                application,
                orderByProperty,
                parent,
                navigationPropertyName
            ) as T
            getKey(EntityTypes.product, EntitySets.products) -> ProductODataViewModel(
                application,
                orderByProperty,
                parent,
                navigationPropertyName
            ) as T
            getKey(EntityTypes.purchaseOrderHeader, EntitySets.purchaseOrderHeaders) -> PurchaseOrderHeaderODataViewModel(
                application,
                orderByProperty,
                parent,
                navigationPropertyName
            ) as T
            getKey(EntityTypes.purchaseOrderItem, EntitySets.purchaseOrderItems) -> PurchaseOrderItemODataViewModel(
                application,
                orderByProperty,
                parent,
                navigationPropertyName
            ) as T
            getKey(EntityTypes.salesOrderHeader, EntitySets.salesOrderHeaders) -> SalesOrderHeaderODataViewModel(
                application,
                orderByProperty,
                parent,
                navigationPropertyName
            ) as T
            getKey(EntityTypes.salesOrderItem, EntitySets.salesOrderItems) -> SalesOrderItemODataViewModel(
                application,
                orderByProperty,
                parent,
                navigationPropertyName
            ) as T
            getKey(EntityTypes.stock, EntitySets.stock) -> StockODataViewModel(
                application,
                orderByProperty,
                parent,
                navigationPropertyName
            ) as T
            getKey(EntityTypes.supplier, EntitySets.suppliers) -> SupplierODataViewModel(
                application,
                orderByProperty,
                parent,
                navigationPropertyName
            ) as T
            else -> { throw UnsupportedOperationException() }
        }
    }
}
