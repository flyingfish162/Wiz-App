package com.sap.wizapp.ui.odata.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.sap.wizapp.R
import com.sap.wizapp.repository.Repository
import com.sap.wizapp.repository.RepositoryFactory
import com.sap.wizapp.util.Converter
import com.sap.wizapp.ui.odata.data.EntityPageSource
import com.sap.wizapp.ui.odata.screens.OperationResult
import com.sap.wizapp.ui.odata.screens.FieldUIState
import com.sap.cloud.mobile.kotlin.odata.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

const val PAGE_SIZE: Int = 20

interface IEntityOperationType

// unspecified type for empty screen while list items selected
enum class EntityOperationType : IEntityOperationType {
    DETAIL, CREATE, UNSPECIFIED
}

enum class EntityUpdateOperationType : IEntityOperationType {
    UPDATE_FROM_LIST, UPDATE_FROM_DETAIL
}

data class ODataUIState(
    val masterEntity: EntityValue? = null,
    val entityOperationType: IEntityOperationType = EntityOperationType.DETAIL,
    val selectedItems: List<EntityValue> = listOf(),
    val isEntityFocused: Boolean = false,
    val editorFiledStates: List<FieldUIState> = listOf() //for editor screen only
)

/**
 * ViewModel for specific odata entity type,
 * including:
 * 1. master entity
 * 2. entity operation type (detail, update, creation)
 * 3. selected entities (long pressed)
 * 4. is entity Focused? => decide show list or detail in non-expand screen, long press on list will clear it, select entity will set it
 **/
abstract class ODataViewModel(
    application: Application,
    val entityType: EntityType,
    val entitySet: EntitySet?,
    private val orderByProperty: Property?,
    open val parent: EntityValue? = null,
    private val navigationPropertyName: String? = null,
) : BaseOperationViewModel(application) {
    //    val entitySet: EntitySet get() = _entitySet
    private val repository: Repository =
        RepositoryFactory.getRepository(entityType, entitySet, orderByProperty)

    val pagingDataState =
        mutableStateOf<Flow<PagingData<EntityValue>>>(flowOf(PagingData.empty()))

    private val _odataUIState = MutableStateFlow(ODataUIState())
    val odataUIState = _odataUIState.asStateFlow()

    private val pagingSourceFactory =
        InvalidatingPagingSourceFactory {
            EntityPageSource(
                entityType,
                entitySet,
                orderByProperty,
                PAGE_SIZE,
                parent,
                navigationPropertyName
            )
        }

    init {
        pagingDataState.value = retrieveEntities()
    }

    private fun retrieveEntities(): Flow<PagingData<EntityValue>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = true),
        pagingSourceFactory = pagingSourceFactory
    ).flow.map {
        it.insertSeparators { before: EntityValue?, after: EntityValue? ->
            if (before == null && after != null) {
                if (_odataUIState.value.masterEntity == null) {
                    setMasterEntity(after)
                }
            }
            return@insertSeparators null
        }
    }.cachedIn(viewModelScope)

    /* Odata UI State related operations */
    private fun selectedItemChange(entity: EntityValue) {
        _odataUIState.update {
            var items = it.selectedItems
            items = if (items.contains(entity)) {
                items - entity
            } else {
                items + entity
            }

            if (items.isNotEmpty()) {
                it.copy(
                    selectedItems = items,
                    isEntityFocused = false,
                    entityOperationType = EntityOperationType.UNSPECIFIED,
                    masterEntity = null
                )
            } else {
                it.copy(
                    selectedItems = items,
                    isEntityFocused = true,
                    entityOperationType = EntityOperationType.DETAIL,
                    masterEntity = entity
                )
            }

        }
    }

    private fun clearSelection() {
        _odataUIState.update {
            it.copy(
                selectedItems = listOf(),
                isEntityFocused = false,
                entityOperationType = EntityOperationType.DETAIL,
                masterEntity = null
            )
        }
    }

    private fun onUpdate() {
        _odataUIState.update {
            val result = if (it.selectedItems.isNotEmpty()) {
                it.copy(
                    entityOperationType = EntityUpdateOperationType.UPDATE_FROM_LIST,
                    masterEntity = it.selectedItems[0],
                    isEntityFocused = true,
                    selectedItems = listOf(),
                    editorFiledStates = populateFiledStates(it.selectedItems[0], true)
                )
            } else {
                it.copy(
                    entityOperationType = EntityUpdateOperationType.UPDATE_FROM_DETAIL,
                    isEntityFocused = true,
                    editorFiledStates = populateFiledStates(it.masterEntity!!, true)
                )
            }
            result
        }
    }

    protected fun populateFiledStates(masterEntity: EntityValue, isEdit: Boolean): List<FieldUIState> {
        return masterEntity.let { entity ->
            entity.entityType.propertyList.toList()
                .filter {
                    //TODO: support complex type
                    val isComputed = it.annotations.has(COMPUTED_ANNOTATION_TERM)
                    // filter navigation, complex type, computed property, and primary key field in edit mode
                    it !is NavigationProperty && it.dataType.isBasic && !isComputed
                        && if (isEdit) !it.isKey else true
                }.map {
                    FieldUIState(
                        entity.getOptionalValue(it)?.toString() ?: "",
                        it,
                        false
                    )
                }.map { validateFieldState(it, it.value) } //perform init validation
        }
    }

    private fun onCreate() {
        _odataUIState.update {
            val emptyEntity = entityType.objectFactory!!.create() as EntityValue
            entitySet?.also { entitySet -> emptyEntity.entitySet = entitySet }
            it.copy(
                entityOperationType = EntityOperationType.CREATE,
                masterEntity = emptyEntity,
                isEntityFocused = true,
                selectedItems = listOf(),
                editorFiledStates = populateFiledStates(emptyEntity, false)
            )
        }
    }

    private fun onEntityDetail(masterEntity: EntityValue?) {
        _odataUIState.update {
            when (it.entityOperationType) {
                EntityUpdateOperationType.UPDATE_FROM_DETAIL, EntityUpdateOperationType.UPDATE_FROM_LIST ->
                    it.copy(
                        entityOperationType = EntityOperationType.DETAIL,
                        isEntityFocused = true,
                        masterEntity = masterEntity
                    )

                EntityOperationType.CREATE ->
                    it.copy(
                        entityOperationType = EntityOperationType.DETAIL,
                        isEntityFocused = false,
                        masterEntity = masterEntity
                    )

                else -> it.copy(
                    entityOperationType = EntityOperationType.DETAIL,
                    masterEntity = masterEntity,
                    selectedItems = listOf(),
                    isEntityFocused = masterEntity != null,
                )
            }
        }
    }

    /* view model APIs*/
    fun onSelectAction(entity: EntityValue) {
        selectedItemChange(entity)
    }

    fun lostEntityFocus() {
        _odataUIState.update { it.copy(isEntityFocused = false) }
    }

    fun exitUpdate() {
        _odataUIState.update {
            if (it.entityOperationType == EntityUpdateOperationType.UPDATE_FROM_DETAIL) {
                it.copy(entityOperationType = EntityOperationType.DETAIL)
            } else {
                it.copy(
                    entityOperationType = EntityOperationType.DETAIL,
                    isEntityFocused = false
                )
            }
        }
    }

    fun exitCreation() {
        _odataUIState.update {
            it.copy(entityOperationType = EntityOperationType.DETAIL)
        }
        lostEntityFocus()
    }

    // delete master entity in details screen
    fun onDeleteAction() {
        if (_odataUIState.value.selectedItems.isNotEmpty()) {
            deleteEntities(_odataUIState.value.selectedItems)
        } else if (_odataUIState.value.masterEntity != null) {
            deleteEntities(listOf(_odataUIState.value.masterEntity!!))
        } else {
            throw IllegalArgumentException("delete with empty selection")
        }
    }

    private fun deleteEntities(entities: List<EntityValue>) {
        viewModelScope.launch(Dispatchers.IO) {
            operationStart()
            when (val operationResult =
                repository.suspendDelete(entities)) {
                is Repository.SuspendOperationResult.SuspendOperationSuccess -> {
                    refreshEntities()
                    operationFinished(result = OperationResult.OperationSuccess("Delete Success"))
                    clearSelection()
                }

                is Repository.SuspendOperationResult.SuspendOperationFail -> {
                    operationFinished(
                        result = OperationResult.OperationFail(
                            operationResult.error.message ?: "Delete fail"
                        )
                    )
                }
            }
        }
    }

    fun onClickAction(entity: EntityValue) {
        onEntityDetail(entity)
    }

    fun refreshEntities() {
        pagingSourceFactory.invalidate()
    }

    fun onEditAction() {
        onUpdate()
    }

    fun onCreateAction() {
        onCreate()
    }

    fun setMasterEntity(entity: EntityValue) {
        Log.d("ODataViewModel", "setMasterEntity: $entity")
        _odataUIState.update {
            it.copy(
                masterEntity = entity,
            )
        }
    }

    fun onSaveAction(
        entity: EntityValue,
        propValuePairs: List<Pair<Property, String>>
    ): List<Converter.ConvertResult.ConvertError> {
        val result = Converter.populateEntityWithPropertyValue(entity, propValuePairs)
        if (result.isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                operationStart()
                val isCreation =
                    _odataUIState.value.entityOperationType == EntityOperationType.CREATE
                when (val operationResult =
                    if (isCreation) {
                        if (parent != null) {
                            if (entity.entityType.isMedia)
                                repository.suspendCreateRelatedEntity(
                                    parent!!,
                                    defaultMediaResource,
                                    entity,
                                    navigationPropertyName!!
                                )
                            else
                                repository.suspendCreateRelatedEntity(
                                    parent!!,
                                    entity,
                                    navigationPropertyName!!
                                )
                        } else {
                            if (entity.entityType.isMedia)
                                repository.suspendCreate(entity, defaultMediaResource)
                            else
                                repository.suspendCreate(entity)
                        }
                    } else {
                        repository.suspendUpdate(entity)
                    }) {
                    is Repository.SuspendOperationResult.SuspendOperationSuccess -> {
                        refreshEntities()
                        operationFinished(result = OperationResult.OperationSuccess("${if (isCreation) "Create" else "Update"} Success"))
                        onEntityDetail(if (isCreation) null else entity)
                    }

                    is Repository.SuspendOperationResult.SuspendOperationFail -> {
                        operationFinished(
                            result = OperationResult.OperationFail(
                                operationResult.error.message
                                    ?: "${if (isCreation) "Create" else "Update"} Fail"
                            )
                        )
                    }
                }
            }
        }
        return result
    }

    //return create action when nav property value is list type or null
    fun onFloatingAdd(): (() -> Unit)? {
        val action = this::onCreateAction
        return parent?.let { parent ->
            return navigationPropertyName?.let {
                val navProp = parent.entityType.getProperty(navigationPropertyName)
                val navValue = parent.getOptionalValue(navProp)
                if(navProp.isEntityList || navValue == null ) action else null
            } ?: action
        } ?: action
    }

    fun updateFieldState(
        fieldStateIndex: Int, newValue: String
    ) {
        val newState = validateFieldState(_odataUIState.value.editorFiledStates[fieldStateIndex], newValue)
        _odataUIState.update {
            val newStates = it.editorFiledStates.toMutableStateList()
            newStates[fieldStateIndex] = newState
            it.copy(
                editorFiledStates = newStates.toList()
            )
        }
    }

    fun validateFieldState(
        fieldUIState: FieldUIState,
        newValue: String
    ): FieldUIState {
        val property = fieldUIState.property
        if (!property.isNullable && newValue.isEmpty()) { // check if mandatory
            return fieldUIState.copy(
                isError = true,
                errorMessage = getApplication<Application>().getString(R.string.mandatory_warning),
                value = newValue
            )
        } else if (newValue.isNotEmpty()) { // check if property type valid input
            val convertResult = Converter.convert(property, newValue)
            if (convertResult is Converter.ConvertResult.ConvertError) {
                return fieldUIState.copy(
                    isError = true,
                    errorMessage = getApplication<Application>().getString(R.string.format_error),
                    value = newValue
                )
            }
        }

        //con max length
        val maxLength = property.maxLength
        return if (maxLength > 0 && newValue.length > maxLength) {
            fieldUIState.copy(value = newValue.substring(0, maxLength), isError = false)
        } else {
            fieldUIState.copy(value = newValue, isError = false)
        }

    }

    //for list view
    open fun getAvatarText(entity: EntityValue?): String {
        val entityPrincipleData =
            orderByProperty?.let { entity?.getOptionalValue(orderByProperty).toString() }
        return if (entityPrincipleData?.isNotEmpty() == true) {
            entityPrincipleData.take(1)
        } else {
            "?"
        }
    }

    open fun getEntityTitle(entity: EntityValue): String {
        val title =
            orderByProperty?.let { entity.getOptionalValue(orderByProperty).toString() }
        return if (title?.isNotEmpty() == true) {
            title
        } else {
            "???"
        }
    }

    private val defaultMediaResource: StreamBase
        get() {
            val inputStream = getApplication<Application>().resources.openRawResource(R.raw.blank)
            val byteStream = ByteStream.fromInput(inputStream)
            byteStream.mediaType = "image/png"
            return byteStream
        }


    companion object {
        const val COMPUTED_ANNOTATION_TERM = "Org.OData.Core.V1.Computed"
    }

}
