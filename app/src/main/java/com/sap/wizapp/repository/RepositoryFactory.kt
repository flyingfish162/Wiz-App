package com.sap.wizapp.repository

import com.sap.wizapp.service.SAPServiceManager
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata.EntitySets
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata.EntityTypes
import com.sap.wizapp.ui.odata.getKey
import com.sap.cloud.mobile.kotlin.odata.EntitySet
import com.sap.cloud.mobile.kotlin.odata.EntityType
import com.sap.cloud.mobile.kotlin.odata.Property

import java.util.WeakHashMap

/*
 * Repository factory to construct repository for an entity set
 */
object RepositoryFactory
/**
 * Construct a RepositoryFactory instance. There should only be one repository factory and used
 * throughout the life of the application to avoid caching entities multiple times.
 */
{
    private val repositories: WeakHashMap<String, Repository> = WeakHashMap()

    /**
     * Construct or return an existing repository for the specified entity set
     * @param entitySet - entity set for which the repository is to be returned
     * @param orderByProperty - if specified, collection will be sorted ascending with this property
     * @return a repository for the entity set
     */
    fun getRepository(entityType: EntityType, entitySet: EntitySet?, orderByProperty: Property?): Repository {
        val eSPMContainer = SAPServiceManager.eSPMContainer!!
        val key = getKey(entityType, entitySet)
        var repository: Repository? = repositories[key]
        if (repository == null) {
            repository = when (key) {
                getKey(EntityTypes.customer, EntitySets.customers) ->
                    Repository(eSPMContainer, EntityTypes.customer, EntitySets.customers, orderByProperty)
                getKey(EntityTypes.productCategory, EntitySets.productCategories) ->
                    Repository(eSPMContainer, EntityTypes.productCategory, EntitySets.productCategories, orderByProperty)
                getKey(EntityTypes.productText, EntitySets.productTexts) ->
                    Repository(eSPMContainer, EntityTypes.productText, EntitySets.productTexts, orderByProperty)
                getKey(EntityTypes.product, EntitySets.products) ->
                    Repository(eSPMContainer, EntityTypes.product, EntitySets.products, orderByProperty)
                getKey(EntityTypes.purchaseOrderHeader, EntitySets.purchaseOrderHeaders) ->
                    Repository(eSPMContainer, EntityTypes.purchaseOrderHeader, EntitySets.purchaseOrderHeaders, orderByProperty)
                getKey(EntityTypes.purchaseOrderItem, EntitySets.purchaseOrderItems) ->
                    Repository(eSPMContainer, EntityTypes.purchaseOrderItem, EntitySets.purchaseOrderItems, orderByProperty)
                getKey(EntityTypes.salesOrderHeader, EntitySets.salesOrderHeaders) ->
                    Repository(eSPMContainer, EntityTypes.salesOrderHeader, EntitySets.salesOrderHeaders, orderByProperty)
                getKey(EntityTypes.salesOrderItem, EntitySets.salesOrderItems) ->
                    Repository(eSPMContainer, EntityTypes.salesOrderItem, EntitySets.salesOrderItems, orderByProperty)
                getKey(EntityTypes.stock, EntitySets.stock) ->
                    Repository(eSPMContainer, EntityTypes.stock, EntitySets.stock, orderByProperty)
                getKey(EntityTypes.supplier, EntitySets.suppliers) ->
                    Repository(eSPMContainer, EntityTypes.supplier, EntitySets.suppliers, orderByProperty)
                else -> throw AssertionError("Fatal error, entity set[$key] missing in generated code")
            }
            repositories[key] = repository
        }
        return repository
    }

    /**
     * Get rid of all cached repositories
     */
    fun reset() {
        repositories.clear()
    }
}
