package com.sap.wizapp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.sap.wizapp.R
import com.sap.wizapp.app.SAPWizardApplication
import com.sap.wizapp.app.WizardFlowActionHandler
import com.sap.wizapp.app.WizardFlowStateListener
import com.sap.wizapp.service.SAPServiceManager
import com.sap.cloud.mobile.fiori.compose.common.PainterBuilder
import com.sap.cloud.mobile.foundation.common.ClientProvider
import com.sap.cloud.mobile.flows.compose.core.FlowContext
import com.sap.cloud.mobile.flows.compose.core.FlowContextRegistry.flowContext
import com.sap.cloud.mobile.flows.compose.db.UserSecureStoreDelegate
import com.sap.cloud.mobile.flows.compose.ext.FlowOptions
import com.sap.cloud.mobile.flows.compose.flows.FlowType
import com.sap.cloud.mobile.flows.compose.flows.FlowUtil
import com.sap.cloud.mobile.flows.compose.flows.FlowUtil.getFinishedFlowName
import com.sap.cloud.mobile.foundation.remotenotification.BasePushService.Companion.NOTIFICATION_ALERT_FROM_SERVER
import com.sap.cloud.mobile.foundation.remotenotification.BasePushService.Companion.NOTIFICATION_ID_FROM_SERVER
import com.sap.cloud.mobile.foundation.remotenotification.BasePushService.Companion.NOTIFICATION_TITLE_FROM_SERVER
import com.sap.cloud.mobile.foundation.remotenotification.ForegroundPushNotificationReady
import com.sap.cloud.mobile.foundation.remotenotification.PushRemoteMessage
import com.sap.cloud.mobile.foundation.mobileservices.SDKInitializer.getService
import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler
import com.sap.wizapp.notification.PushService
import com.sap.cloud.mobile.foundation.configurationprovider.FileConfigurationProvider
import com.sap.cloud.mobile.foundation.configurationprovider.ProviderConfiguration
import com.sap.cloud.mobile.foundation.configurationprovider.ProviderInputs
import com.sap.cloud.mobile.foundation.mobileservices.ApplicationStates
import com.sap.cloud.mobile.foundation.mobileservices.TimeoutLockService
import com.sap.cloud.mobile.foundation.model.AppConfig
import com.sap.cloud.mobile.onboarding.compose.settings.CustomScreenSettings
import com.sap.cloud.mobile.onboarding.compose.settings.LaunchScreenContentSettings
import com.sap.cloud.mobile.onboarding.compose.settings.LaunchScreenSettings
import com.sap.cloud.mobile.onboarding.compose.settings.QRCodeReaderScreenSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.MainScope
import org.slf4j.LoggerFactory

class WelcomeActivity : ComponentActivity() {

    private lateinit var providerConfiguration: ProviderConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        handlePushNotification()
        val warning = intent.getStringExtra(EXTRA_WAINING_KEY)
        setContent {
            var showError by remember { mutableStateOf(false) }
            var showWarning by remember { mutableStateOf(warning != null) }
            if(showWarning) {
                AlertDialogComponent(
                    title = getString(com.sap.cloud.mobile.onboarding.compose.R.string.dialog_info_title),
                    text = warning!!,
                    onPositiveButtonClick = {
                        showWarning = false
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    if (UserSecureStoreDelegate.getInstance().isUserStoreOpen()) {
                        // close itself if application is unlocked without starting onboard
                        this@WelcomeActivity.finish()
                    }
                    providerConfiguration = loadConfiguration(this@WelcomeActivity)
                    if (providerConfiguration.providerSuccess) {
                        val appConfig =
                            AppConfig.createAppConfigFromJsonString(providerConfiguration.configuration.toString())
                        startOnboarding(this@WelcomeActivity, appConfig)
                    } else {
                        showError = true
                    }
                }
            }

            if (showError) {
                val errorMessage = providerConfiguration.returnError?.errorMessage
                val dialogMessage = if (errorMessage != null) stringResource(
                    R.string.config_loader_on_error_description,
                    "com.sap.configuration.provider.fileconfiguration",
                    errorMessage
                ) else stringResource(R.string.config_loader_complete_error_description)

                AlertDialogComponent(
                    text = dialogMessage,
                    onPositiveButtonClick = {
                        this.finish()
                    }
                )
            }
        }
    }

    private fun handlePushNotification() {
        val notificationID = intent.getStringExtra(NOTIFICATION_ID_FROM_SERVER)
        if(notificationID != null) {
            val message = PushRemoteMessage().apply {
                alert = intent.getStringExtra(NOTIFICATION_ALERT_FROM_SERVER)
                title = intent.getStringExtra(NOTIFICATION_TITLE_FROM_SERVER)
                this.notificationID = notificationID
            }

            val service = getService(PushService().getPushService()::class)
            service!!.storeNotificationMessage(false, message, object :
                ForegroundPushNotificationReady {
                override fun onConditionReady(): Boolean {
                    return AppLifecycleCallbackHandler.getInstance().activity?.javaClass?.name?.contains("WelcomeActivity") != true
                }
            })
        }
    }

    companion object {
        internal val logger = LoggerFactory.getLogger(WelcomeActivity::class.java)
        const val EXTRA_WAINING_KEY = "EXTRA_WAINING_KEY"
    }

}

private fun loadConfiguration(context: Context): ProviderConfiguration {
    return FileConfigurationProvider(
        context, "sap_mobile_services"
    ).provideConfiguration(
        ProviderInputs()
    )
}

fun startOnboarding(context: Context, appConfig: AppConfig) {
    TimeoutLockService.updateApplicationLockState(true)
    WelcomeActivity.logger.debug("Before starting flow, lock state: {}", ApplicationStates.applicationLocked)
    FlowUtil.startFlow(
        context,
        flowContext = getOnboardingFlowContext(context, appConfig)
    ) { resultCode, data ->
        if (resultCode == Activity.RESULT_OK) {

            SAPServiceManager.openODataStore {
                launchMainBusinessActivity(context)
            }
            PainterBuilder.setupImageLoader(
                context, ClientProvider.get()
            )
            WelcomeActivity.logger.debug("After flow, lock state: {}",  ApplicationStates.applicationLocked)
        } else {
            startOnboarding(context, appConfig)
        }
    }
}

private fun prepareScreenSettings() =
    CustomScreenSettings(
        launchScreenSettings = LaunchScreenSettings(
            titleResId = R.string.application_name,
            contentSettings = LaunchScreenContentSettings(
                contentImage = R.drawable.ic_sap_icon_sdk_transparent,
                title = R.string.launch_screen_content_title,
                content = R.string.launch_screen_content_body,
            ),
            bottomPrivacyUrl = "http://www.sap.com"
        ),
        qrCodeReaderScreenSettings = QRCodeReaderScreenSettings(
            scanInternal = 50L
        )
    )

/** Returns the flow context for onboarding.*/
fun getOnboardingFlowContext(context: Context, appConfig: AppConfig) = FlowContext(
    appConfig = appConfig,
    flowActionHandler = WizardFlowActionHandler(context.applicationContext as SAPWizardApplication),
    flowStateListener = WizardFlowStateListener(context.applicationContext as SAPWizardApplication),
    flowOptions = FlowOptions(
//      oAuthAuthenticationOption = OAuth2WebOption.WEB_VIEW,
        useDefaultEulaScreen = false,
        screenSettings = prepareScreenSettings(),
        fullScreen = false
    )
)

fun launchWelcomeActivity(context: Context, updateIntent: ((Intent) -> Unit) = {}) {
    val intent = Intent(context, WelcomeActivity::class.java).apply {
        addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
    }.apply(updateIntent)
    context.startActivity(intent)
}

fun launchMainBusinessActivity(context: Context) {
    val intent = Intent(context, MainBusinessActivity::class.java).apply {
        addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
    }
    context.startActivity(intent)
}
