package com.sap.wizapp.app

import android.app.Application
import com.sap.cloud.mobile.foundation.app.security.ClipboardProtectionService
import com.sap.wizapp.data.SharedPreferenceRepository
import com.sap.wizapp.repository.RepositoryFactory
import com.sap.cloud.mobile.foundation.app.security.LockAndWipeService
import android.content.Intent
import com.sap.wizapp.R
import com.sap.wizapp.ui.WelcomeActivity.Companion.EXTRA_WAINING_KEY
import com.sap.wizapp.ui.launchWelcomeActivity
import com.sap.cloud.mobile.flows.compose.core.FlowContextRegistry
import com.sap.cloud.mobile.flows.compose.flows.FlowType
import com.sap.cloud.mobile.flows.compose.flows.FlowUtil
import com.sap.cloud.mobile.flows.compose.flows.FlowUtil.populateCustomBundle
import com.sap.cloud.mobile.flows.compose.flows.FlowUtil.skipConfirmForDeleteRegistrationFlow
import com.sap.cloud.mobile.foundation.app.security.ServerBlockType
import com.sap.cloud.mobile.foundation.mobileservices.MobileService
import com.sap.cloud.mobile.foundation.mobileservices.SDKInitializer
import com.sap.wizapp.BuildConfig
import com.sap.cloud.mobile.permission.request.tracker.PermissionRequestTracker
import com.sap.cloud.mobile.foundation.logging.LoggingService
import com.sap.cloud.mobile.foundation.settings.policies.LogPolicy
import com.sap.wizapp.notification.PushService
import com.sap.cloud.mobile.foundation.remotenotification.*
import android.content.res.Configuration
import androidx.compose.runtime.mutableStateOf
import com.sap.cloud.mobile.foundation.usage.UsageService
import com.sap.cloud.mobile.foundation.crash.CrashService
import com.sap.cloud.mobile.foundation.theme.ThemeDownloadService


class SAPWizardApplication: Application() {


    override fun onCreate() {
        super.onCreate()
        initServices()
        if (BuildConfig.FLAVOR == "tencentAppStoreforChinaMarket") {
            PermissionRequestTracker.setPermissionRequestTrackingEnabled(this,true)
        } else {
            PermissionRequestTracker.setPermissionRequestTrackingEnabled(this,false)
        }
    }

    /**
     * Clears all user-specific data and configuration from the application, essentially resetting it to its initial
     * state.
     *
     * If client code wants to handle the reset logic of a service, here is an example:
     *
     *   SDKInitializer.resetServices { service ->
     *       return@resetServices if( service is PushService ) {
     *           PushService.unregisterPushSync(object: CallbackListener {
     *               override fun onSuccess() {
     *               }
     *
     *               override fun onError(p0: Throwable) {
     *               }
     *           })
     *           true
     *       } else {
     *           false
     *       }
     *   }
     */
    suspend fun resetApplication() {
        SharedPreferenceRepository(this).resetSharedPreference()
        RepositoryFactory.reset()
        SDKInitializer.resetServices()

    }

    private fun initServices() {
        val services = mutableListOf<MobileService>()
        services.add(ThemeDownloadService(this))
        services.add(ClipboardProtectionService(true))
        services.add(LockAndWipeService {
            when (it) {
                ServerBlockType.TRAFFIC_REG_WIPED -> {
                    FlowUtil.startFlow(
                        context = this,
                        flowContext = FlowContextRegistry.flowContext.copy(
                            flowType = FlowType.DeleteRegistration
                        ),
                        updateIntent = { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.populateCustomBundle {
                                skipConfirmForDeleteRegistrationFlow(true)
                            }
                        }
                    ) { _, _ ->
                        launchWelcomeActivity(this@SAPWizardApplication) { intent ->
                            intent.putExtra(
                                EXTRA_WAINING_KEY,
                                getString(R.string.warning_wiped_msg)
                            )
                        }
                    }
                }

                ServerBlockType.TRAFFIC_REG_LOCKED -> {
                    launchWelcomeActivity(this@SAPWizardApplication) { intent ->
                        intent.putExtra(
                            EXTRA_WAINING_KEY,
                            getString(com.sap.cloud.mobile.flows.compose.R.string.reg_locked_by_policy)
                        )
                    }
                }

                else -> {
                    launchWelcomeActivity(this@SAPWizardApplication) { intent ->
                        intent.putExtra(
                            EXTRA_WAINING_KEY,
                            getString(com.sap.cloud.mobile.flows.compose.R.string.reg_locked_by_admin)
                        )
                    }
                }
            }
        })
        services.add(LoggingService(autoUpload = false).apply {
            policy = LogPolicy(logLevel = "WARN", entryExpiry = 0, maxFileNumber = 4)
            logToConsole = true
        })
        services.add(UsageService())
        services.add(CrashService(true))
        val parameters = RemoteNotificationParameters.Builder()
            .userLocale(resources.configuration.locales.get(0).toString())
            .build()

        var pushService: PushService = PushService()
        services.add(pushService.getInitializedPushService(parameters, this@SAPWizardApplication, packageManager, packageName))

        SDKInitializer.start(this, * services.toTypedArray())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val parameters = RemoteNotificationParameters.Builder()
            .userLocale(newConfig.locales.get(0).toString())
            .build()
        var pushService: PushService = PushService()
        SDKInitializer.getService(pushService.getPushService()::class)?.updateRegistration(
            parameters,
            object : RemoteNotificationClient.CallbackListener {
                override fun onSuccess() {
                    //TO-DO
                }

                override fun onError(e: Throwable) {
                    //TO-DO
                }
            })
    }

    companion object {
    }
}
