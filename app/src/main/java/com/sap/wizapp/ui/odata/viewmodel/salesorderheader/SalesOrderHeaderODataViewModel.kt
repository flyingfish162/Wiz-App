package com.sap.wizapp.ui.odata.viewmodel.salesorderheader

import android.app.Application
import com.sap.wizapp.ui.odata.viewmodel.ODataViewModel
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata
import com.sap.cloud.mobile.kotlin.odata.EntityValue
import com.sap.cloud.mobile.kotlin.odata.Property

class SalesOrderHeaderODataViewModel(
    application: Application,
    private val orderByProperty: Property?,
    override val parent: EntityValue? = null,
    private val navigationPropertyName: String? = null,
) : ODataViewModel(
    application,
    ESPMContainerMetadata.EntityTypes.salesOrderHeader,
    ESPMContainerMetadata.EntitySets.salesOrderHeaders,
    orderByProperty,
    parent,
    navigationPropertyName
) {

//    override fun getAvatarText(entity: EntityValue?): String {
//        val customer = entity as Customer?
//        return customer?.let { "${it.lastName?.first() ?: '?'}${it.firstName?.first() ?: '?'}" }
//            ?: "??"
//    }
}
