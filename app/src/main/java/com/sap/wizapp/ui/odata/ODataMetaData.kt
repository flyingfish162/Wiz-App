package com.sap.wizapp.ui.odata

import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata.EntitySets
import com.sap.cloud.mobile.kotlin.odata.EntitySet
import com.sap.cloud.mobile.kotlin.odata.EntityType
import com.sap.cloud.mobile.kotlin.odata.Property
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata.EntityTypes

enum class EntityMetaData(
    val entityType: EntityType,
    val orderByProperty: Property?,
    val entitySet: EntitySet? = null,
) {
    Customers(
        EntityTypes.customer,
        com.sap.cloud.android.odata.espmcontainer.Customer.city,
        EntitySets.customers,
        ),
    ProductCategories(
        EntityTypes.productCategory,
        com.sap.cloud.android.odata.espmcontainer.ProductCategory.categoryName,
        EntitySets.productCategories,
        ),
    ProductTexts(
        EntityTypes.productText,
        com.sap.cloud.android.odata.espmcontainer.ProductText.language,
        EntitySets.productTexts,
        ),
    Products(
        EntityTypes.product,
        com.sap.cloud.android.odata.espmcontainer.Product.category,
        EntitySets.products,
        ),
    PurchaseOrderHeaders(
        EntityTypes.purchaseOrderHeader,
        com.sap.cloud.android.odata.espmcontainer.PurchaseOrderHeader.currencyCode,
        EntitySets.purchaseOrderHeaders,
        ),
    PurchaseOrderItems(
        EntityTypes.purchaseOrderItem,
        com.sap.cloud.android.odata.espmcontainer.PurchaseOrderItem.currencyCode,
        EntitySets.purchaseOrderItems,
        ),
    SalesOrderHeaders(
        EntityTypes.salesOrderHeader,
        com.sap.cloud.android.odata.espmcontainer.SalesOrderHeader.createdAt,
        EntitySets.salesOrderHeaders,
        ),
    SalesOrderItems(
        EntityTypes.salesOrderItem,
        com.sap.cloud.android.odata.espmcontainer.SalesOrderItem.currencyCode,
        EntitySets.salesOrderItems,
        ),
    Stock(
        EntityTypes.stock,
        com.sap.cloud.android.odata.espmcontainer.Stock.lotSize,
        EntitySets.stock,
        ),
    Suppliers(
        EntityTypes.supplier,
        com.sap.cloud.android.odata.espmcontainer.Supplier.city,
        EntitySets.suppliers,
        ),
}

fun getOrderByProperty(entityType: EntityType): Property? {
    return EntityMetaData.entries.first { it.entityType == entityType }.orderByProperty
}

fun getKey(entityType: EntityType, entitySet: EntitySet? = null): String {
    return "${entitySet?.localName}_${entityType.localName}"
}