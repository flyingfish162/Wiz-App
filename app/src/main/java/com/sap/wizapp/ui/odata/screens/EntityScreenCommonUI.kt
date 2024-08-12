package com.sap.wizapp.ui.odata.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata
import com.sap.wizapp.R
import com.sap.wizapp.ui.AlertDialogComponent
import com.sap.wizapp.ui.odata.ActionItem
import com.sap.wizapp.ui.odata.OverflowMode
import com.sap.wizapp.ui.odata.viewmodel.ODataViewModel
import com.sap.cloud.mobile.kotlin.odata.EntityValue
import com.sap.cloud.mobile.kotlin.odata.Property
import com.sap.cloud.mobile.fiori.compose.avatar.model.FioriAvatarData
import com.sap.cloud.mobile.fiori.compose.common.FioriIcon
import com.sap.cloud.mobile.fiori.compose.common.FioriImage
import com.sap.cloud.mobile.fiori.compose.objectheader.model.*

@Composable
fun DeleteEntityWithConfirmation(viewModel: ODataViewModel, confirmState: MutableState<Boolean>) {
    val context = LocalContext.current

    if (confirmState.value) {
        AlertDialogComponent(title = context.getString(R.string.delete_dialog_title),
            text = context.getString(R.string.delete_one_item),
            onNegativeButtonClick = { confirmState.value = false },
            positiveButtonText = context.getString(R.string.delete),
            onPositiveButtonClick = {
                confirmState.value = false
                viewModel.onDeleteAction()
            })
    }
}

@Composable
fun LeaveEditorWithConfirmation(onLeave: () -> Unit, confirmState: MutableState<Boolean>) {
    val context = LocalContext.current
    if(confirmState.value) {
        AlertDialogComponent(
            title = context.getString(R.string.before_navigation_dialog_title),
            text = context.getString(R.string.before_navigation_dialog_message),
            onNegativeButtonClick = { confirmState.value = false },
            positiveButtonText = context.getString(R.string.before_navigation_dialog_positive_button),
            negativeButtonText = context.getString(R.string.before_navigation_dialog_negative_button),
            onPositiveButtonClick = {
                confirmState.value = false
                onLeave()
            }
        )
    }
}

@Composable
fun getSelectedItemActionsList(
    navigateToHome: () -> Unit,
    viewModel: ODataViewModel,
    deleteState: MutableState<Boolean>
): List<ActionItem> {
    val uiState = viewModel.odataUIState.collectAsState()
    return when (uiState.value.selectedItems.size) {
        0 -> listOf(
            ActionItem(
                nameRes = R.string.menu_search,
                iconRes = if (viewModel.showSearchInput.collectAsState().value) R.drawable.ic_sap_icon_decline else R.drawable.ic_search_icon,
                overflowMode = if(viewModel.entityType == ESPMContainerMetadata.EntityTypes.productCategory) OverflowMode.IF_NECESSARY else OverflowMode.NOT_SHOWN,
                doAction = if (viewModel.showSearchInput.collectAsState().value) viewModel::hideSearchInput else viewModel::showSearchInput
            ), ActionItem(
                nameRes = R.string.menu_home,
                iconRes = R.drawable.ic_sap_icon_home,
                overflowMode = if(viewModel.entityType == ESPMContainerMetadata.EntityTypes.productCategory) OverflowMode.NOT_SHOWN else OverflowMode.IF_NECESSARY,
                doAction = navigateToHome
            ), ActionItem(
                nameRes = R.string.menu_refresh,
                iconRes = R.drawable.ic_sap_icon_refresh,
                overflowMode = OverflowMode.IF_NECESSARY,
                doAction = viewModel::refreshEntities
            )
        )

        else -> getSelectedItemActionsList(
            viewModel,
            deleteState
        )
    }
}

@Composable
fun getSelectedItemActionsList(
    viewModel: ODataViewModel,
    deleteState: MutableState<Boolean>
): List<ActionItem> {
    val uiState = viewModel.odataUIState.collectAsState()
    return when (uiState.value.selectedItems.size) {
        0 -> listOf() //should not happen
        1 -> listOf(
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
                doAction = { deleteState.value = true }
            ),
        )

        else -> listOf(
            ActionItem(
                nameRes = R.string.menu_delete,
                iconRes = R.drawable.ic_sap_icon_delete,
                overflowMode = OverflowMode.IF_NECESSARY,
                doAction = { deleteState.value = true }
            ),
        )
    }
}

data class StateIcon(@DrawableRes val icon: Int, @StringRes val desc: Int)

fun getEntityStateIcon(it: EntityValue): StateIcon {
    return when {
        it.inErrorState -> StateIcon(
            R.drawable.ic_error_state, R.string.error_state
        )

        it.isUpdated -> StateIcon(
            R.drawable.ic_updated_state, R.string.updated_state
        )
        it.isLocal -> StateIcon(
            R.drawable.ic_local_state, R.string.local_state
        )
        else ->
            StateIcon(
                R.drawable.ic_download_state, R.string.download_state
            )
    }

}

sealed interface ResultOf<out Bitmap> {
    data object Loading : ResultOf<Nothing>
    data class Success(val bitmap: Bitmap) : ResultOf<Bitmap>
    data object Failure : ResultOf<Nothing>
}


fun defaultObjectHeaderData(
    title: String,
    imageByteArray: ByteArray? = null,
    imageUrl: String? = null,
    imageChars: String? = null
): FioriObjectHeaderData {

    val content = FioriObjectHeaderData(
        title
    )

    content.detailImage = imageByteArray?.let {
        if (it.isNotEmpty()) {
            FioriAvatarData(
                FioriImage(
                    bitmap = BitmapFactory.decodeByteArray(it, 0, it.size),
                    contentDescription = "Detail image"
                )
            )
        } else {
            //load image fail
            imageChars?.let {char ->
                FioriAvatarData(
                    text = char,
                    textFontSize = 24.sp,
                )
            }
        }
    } ?: imageUrl?.let {
        FioriAvatarData(
            FioriImage(
                url = it,
                contentDescription = "Detail image"
            )
        )
    } ?: imageChars?.let {
        FioriAvatarData(
            text = it,
            textFontSize = 24.sp,
        )
    } ?: FioriAvatarData(
        FioriImage(
            resId = R.drawable.ic_sync,
            contentDescription = "Loading image"
        )
    )

    content.subtitle = "This is a subtitle that can take up to a maximum of three lines."

    content.accessoryKpi = "accKpi"
    content.accessoryKpiLabel = "accKpiLabel"



    content.status = FioriObjectHeaderStatusData(
        label = "Status",
        icon = FioriIcon(
            resId = R.drawable.ic_sap_icon_home,
            contentDescription = "Positive status icon",
        ),
        type = FioriObjectHeaderStatusType.Positive,
        isIconAtStart = true
    )
    content.subStatus = FioriObjectHeaderStatusData(
        label = "SubStatus",
        icon = FioriIcon(
            resId = R.drawable.ic_error_state,
            contentDescription = "Critical status icon",
        ),
        type = FioriObjectHeaderStatusType.Critical,
        isIconAtStart = false
    )


    content.labelItems = listOf(
        FioriObjectHeaderLabelItemData(
            label = "Attribute1"
        ),
        FioriObjectHeaderLabelItemData(
            label = "Attribute2",
            icon = FioriIcon(
                resId = R.drawable.ic_sap_icon_home
            ),
            isIconAtStart = false
        ),
        FioriObjectHeaderLabelItemData(
            label = "Subtitle"
        ),
        FioriObjectHeaderLabelItemData(
            label = "3/28/2022",
            icon = FioriIcon(
                resId = R.drawable.ic_sap_icon_delete
            )
        )
    )


    content.description =
        "description show here"


    return content
}

data class FieldUIState(
    val value: String,
    val property: Property,
    val isError: Boolean = false,
    val errorMessage: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (other !is FieldUIState) return false
        return (this.value == other.value && this.property.name == other.property.name && this.isError == other.isError && errorMessage == other.errorMessage)
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + property.hashCode()
        result = 31 * result + isError.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }
}
