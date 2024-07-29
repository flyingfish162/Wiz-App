package com.sap.wizapp.repository

import com.sap.wizapp.ui.odata.data.EntityMediaResource

import com.sap.cloud.android.odata.espmcontainer.ESPMContainer
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata
import com.sap.cloud.android.odata.espmcontainer.Product

import com.sap.cloud.mobile.kotlin.odata.*
import com.sap.cloud.mobile.kotlin.odata.http.HttpHeaders

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

import org.slf4j.LoggerFactory


/**
 * Repository with specific EntitySet as parameter.
 * In other words, each entity set has its own repository and an in-memory store of all the entities
 * of that type.
 * Repository exposed the list of entities as paging data flow
 * @param eSPMContainer OData service
 * @param entityType entity type associated with this repository
 * @param entitySet entity set associated with this repository
 * @param orderByProperty used to order the collection retrieved from OData service
 */
class Repository(
        private val eSPMContainer: ESPMContainer,
        private val entityType: EntityType,
        private val entitySet: EntitySet?,
        private val orderByProperty: Property?) {
    /*
     * Indicate if metadata=full parameter needs to be set during query for the entity set
     * V4 and higher OData version services do not return metadata as part of the result preventing the
     * the construction of download url for use by Glide.
     */
    private var needFullMetadata = false

    /**
     * Return a suitable HttpHeader based on whether full metadata parameter is required
     * @return HttpHeader for query
     */
    private val httpHeaders: HttpHeaders
        get() {
            val httpHeaders: HttpHeaders
            if (needFullMetadata) {
                httpHeaders = HttpHeaders()
                httpHeaders.set("Accept", "application/json;odata.metadata=full")
            } else {
                httpHeaders = HttpHeaders.empty
            }
            return httpHeaders
        }
    
    init {
        if (EntityMediaResource.isV4(eSPMContainer.metadata.versionCode) && EntityMediaResource.hasMediaResources(entityType)) {
            needFullMetadata = true
        }
    }

    suspend fun read(pageSize: Int = 40, page: Int = 0): Flow<List<EntityValue>> {
        return entitySet?.let{
            flow {
                val dataQuery = DataQuery().from(entitySet).page(pageSize).skip(page * pageSize)
                orderByProperty?.also {
                    dataQuery.orderBy(it, SortOrder.ASCENDING)
                    if (entitySet.entityType == ESPMContainerMetadata.EntityTypes.product) {
                        dataQuery.thenBy(Product.name, SortOrder.ASCENDING)
                    }
                }
                try {
                    val result =  eSPMContainer.executeQuery(dataQuery, httpHeaders).getEntityList().toList()
                    emit(result)
                } catch (error: Exception) {
                    LOGGER.error("Error encountered during fetch of Category collection", error)
                    emit(listOf())
                }
            }.flowOn(Dispatchers.IO)
        } ?: throw IllegalArgumentException("Read data against containment property entity directly!")
    }

    suspend fun read(
        parent: EntityValue,
        navPropertyName: String,
        pageSize: Int = 40,
        page: Int = 0
    ): Flow<List<EntityValue>> = flow {
        val navigationProperty = parent.entityType.getProperty(navPropertyName)
        val dataQuery = DataQuery()
        if (navigationProperty.isCollection) {
            dataQuery.page(pageSize).skip(page * pageSize)
            orderByProperty?.also {
                dataQuery.orderBy(
                    orderByProperty,
                    SortOrder.ASCENDING
                )
            }
        }

        val entities = mutableListOf<EntityValue>()
        try {
            eSPMContainer.loadProperty(navigationProperty, parent, dataQuery, httpHeaders)
            val relatedData = parent.getOptionalValue(navigationProperty)

            when (navigationProperty.dataType.code) {
                DataType.ENTITY_VALUE_LIST -> entities.addAll((relatedData as EntityValueList?)!!.toList())
                DataType.ENTITY_VALUE -> if (relatedData != null) {
                    entities.add(relatedData as EntityValue)
                }
            }
        } catch (error: Exception) {
            LOGGER.error("Error encountered during fetch of Category collection", error)
        }

        emit(entities)
    }.flowOn(Dispatchers.IO)
    
    sealed class SuspendOperationResult {
        data class SuspendOperationSuccess(val newEntity: EntityValue? = null) :
            SuspendOperationResult()

        data class SuspendOperationFail(val error: Exception) : SuspendOperationResult()
    }

    suspend fun suspendCreate(newEntity: EntityValue, media: StreamBase): SuspendOperationResult {
        if (newEntity.entityType.isMedia) {
            return try {
                eSPMContainer.createMedia(newEntity, media)
                SuspendOperationResult.SuspendOperationSuccess(
                    newEntity
                )
            } catch (error: Exception) {
                LOGGER.error("Media Linked Entity creation failed.", error)
                SuspendOperationResult.SuspendOperationFail(error)
            }
        } else throw IllegalArgumentException("${newEntity.entityType} is not a media type!")
    }
    
    suspend fun suspendCreate(newEntity: EntityValue): SuspendOperationResult {
        if (newEntity.entityType.isMedia) {
            return SuspendOperationResult.SuspendOperationFail(IllegalStateException("Specify media resource for Media Linked Entity"))
        }

        return try {
            eSPMContainer.createEntity(newEntity)
            SuspendOperationResult.SuspendOperationSuccess(
                newEntity
            )
        } catch (error: Exception) {
            LOGGER.error("Entity creation failed:", error)
            SuspendOperationResult.SuspendOperationFail(error)
        }
    }

    suspend fun suspendCreateRelatedEntity(parent: EntityValue, newEntity: EntityValue, navPropName: String): SuspendOperationResult {
        val navProp = parent.entityType.getProperty(navPropName)
        return try {
            eSPMContainer.createRelatedEntity(newEntity, parent, navProp)
            SuspendOperationResult.SuspendOperationSuccess(
                newEntity
            )
        } catch (error: Exception) {
            LOGGER.error("Navigation child entity creation failed:", error)
            SuspendOperationResult.SuspendOperationFail(error)
        }
    }

    suspend fun suspendCreateRelatedEntity(parent: EntityValue, media: StreamBase, newEntity: EntityValue, navPropName: String): SuspendOperationResult {
        val navProp = parent.entityType.getProperty(navPropName)
        return try {
            eSPMContainer.createRelatedMedia(newEntity, media, parent, navProp)
            SuspendOperationResult.SuspendOperationSuccess(
                newEntity
            )
        } catch (error: Exception) {
            LOGGER.error("Navigation child media entity creation failed:", error)
            SuspendOperationResult.SuspendOperationFail(error)
        }
    }

    suspend fun suspendUpdate(updateEntity: EntityValue): SuspendOperationResult {
        return try {
            eSPMContainer.updateEntity(updateEntity)
            SuspendOperationResult.SuspendOperationSuccess(
                updateEntity
            )
        } catch (error: Exception) {
            LOGGER.error("Entity update failed:", error)
            SuspendOperationResult.SuspendOperationFail(error)
        }
    }

    suspend fun suspendDelete(deleteEntities: List<EntityValue>): SuspendOperationResult {
        val deleteChangeSet = ChangeSet()
        for (entityToDelete in deleteEntities) {
            deleteChangeSet.deleteEntity(entityToDelete)
        }

        return try {
            eSPMContainer.applyChanges(deleteChangeSet)
            SuspendOperationResult.SuspendOperationSuccess()
        } catch (error: Exception) {
            LOGGER.error("Entities delete failed:", error)
            SuspendOperationResult.SuspendOperationFail(error)
        }
    }



    companion object {
        private val LOGGER = LoggerFactory.getLogger(Repository::class.java)
    }
}
