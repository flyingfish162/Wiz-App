package com.sap.wizapp.service

import com.sap.cloud.mobile.foundation.model.AppConfig
import com.sap.cloud.android.odata.espmcontainer.ESPMContainer
import com.sap.cloud.mobile.foundation.common.ClientProvider
import com.sap.cloud.mobile.kotlin.odata.OnlineODataProvider
import com.sap.cloud.mobile.kotlin.odata.http.OKHttpHandler

object SAPServiceManager {
	
    private const val CONNECTION_ID_ESPMCONTAINER: String = "com.sap.edm.sampleservice.v4"
	
	private lateinit var appConfig: AppConfig
	fun initSAPServiceManager(config: AppConfig) {
	        appConfig = config
	}

    var serviceRoot: String = ""
        private set
        get() {
            return (eSPMContainer?.provider as OnlineODataProvider).serviceRoot
        }

    var eSPMContainer: ESPMContainer? = null
        private set
        get() {
            return field ?: throw IllegalStateException("SAPServiceManager was not initialized")
        }

    fun openODataStore(callback: () -> Unit = {}) {
		appConfig.serviceUrl.let { _serviceURL ->
		    eSPMContainer = ESPMContainer (
		        OnlineODataProvider("SAPService", _serviceURL + CONNECTION_ID_ESPMCONTAINER).apply {
		            networkOptions.httpHandler = OKHttpHandler(ClientProvider.get())
		            serviceOptions.checkVersion = false
		            serviceOptions.requiresType = true
		            serviceOptions.cacheMetadata = false
		        }
		    )
		}
        callback.invoke()
    }

}
