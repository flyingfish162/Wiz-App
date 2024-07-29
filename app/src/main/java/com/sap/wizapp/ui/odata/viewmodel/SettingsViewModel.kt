package com.sap.wizapp.ui.odata.viewmodel

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import ch.qos.logback.classic.Level
import com.sap.wizapp.R
import com.sap.wizapp.data.SharedPreferenceRepository
import com.sap.wizapp.ui.odata.screens.OperationResult
import com.sap.cloud.mobile.flows.compose.core.ConsentType
import com.sap.cloud.mobile.flows.compose.db.UserSecureStoreDelegate
import com.sap.cloud.mobile.foundation.crash.CrashService
import com.sap.cloud.mobile.foundation.logging.LoggingService
import com.sap.cloud.mobile.foundation.mobileservices.SDKInitializer
import com.sap.cloud.mobile.foundation.mobileservices.ServiceListener
import com.sap.cloud.mobile.foundation.mobileservices.ServiceResult
import com.sap.cloud.mobile.foundation.settings.policies.LogPolicy
import com.sap.cloud.mobile.foundation.usage.UsageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import com.sap.wizapp.notification.PushService
import com.sap.cloud.mobile.foundation.remotenotification.RemoteNotificationClient

data class SettingUIState(
    val level: Level = Level.OFF,
    val consentUsageCollection: Boolean = true,
    val topics: MutableList<SettingsViewModel.TopicOption> = mutableListOf(),
    val consentCrashReportCollection: Boolean = true
)

class SettingsViewModel(application: Application) : BaseOperationViewModel(application) {
    private val sharedPreferenceRepository = SharedPreferenceRepository(getApplication())
    private val preferencesFlow = sharedPreferenceRepository.userPreferencesFlow

    val supportUsage = SDKInitializer.getService(UsageService::class) != null
    val supportCrashReport = SDKInitializer.getService(CrashService::class) != null
    val supportLogging = SDKInitializer.getService(LoggingService::class) != null

    private val _settingUIState = MutableStateFlow(SettingUIState())
    val settingUIState = _settingUIState.asStateFlow()

    init {
        //init from shared preference
        viewModelScope.launch(Dispatchers.Default) {
            preferencesFlow.collect { userReference ->
                logger.debug("get preference as {}", userReference.logSetting)
                _settingUIState.update { uiState ->
                    uiState.copy(level = LogPolicy.getLogLevel(userReference.logSetting))
                }
            }
        }

        val consentUsage = UserSecureStoreDelegate.getInstance().getConsentStatus(ConsentType.USAGE)
        val consentCrashReport =
            UserSecureStoreDelegate.getInstance().getConsentStatus(ConsentType.CRASH_REPORT)
        logger.debug(
            "init consent data : consentUsage {}, consentCrashReport {}",
            consentUsage,
            consentCrashReport
        )
        _settingUIState.update { uiState ->
            uiState.copy(
                consentUsageCollection = consentUsage,
                consentCrashReportCollection = consentCrashReport
            )
        }

        //Push Topics
        val topicOptions = settingUIState.value.topics
        topicOptions.add(TopicOption("topic1", "Topic 1", false))
        topicOptions.add(TopicOption("topic2", "Topic 2", false))
        topicOptions.add(TopicOption("topic3", "Topic 3", false))
        val pushService: PushService = PushService()
        SDKInitializer.getService(pushService.getPushService()::class)?.getAllSubscribedTopics(object: RemoteNotificationClient.CallbackListenerWithResult {
            override fun onSuccess(result: ServiceResult.SUCCESS<Array<String?>>) {
                result.data?.forEach {
                    topicOptions.forEach { topicOption ->
                        if (topicOption.id == it) {
                            topicOption.state = true
                        }
                    }
                }
                _settingUIState.update { uiState ->
                    uiState.copy(
                        topics = topicOptions
                    )
                }
            }
            override fun onError(e: Throwable) {
                logger.error("Push Topics: $e")
            }
        })
    }

    fun updateConsents(consentType: ConsentType, consent: Boolean) {
        logger.debug("update consent type {}, consent : {}", consentType, consent)
        viewModelScope.launch(Dispatchers.Default) {
            UserSecureStoreDelegate.getInstance().updateConsentStatus(consentType, consent)
        }
        when (consentType) {
            ConsentType.CRASH_REPORT -> {
                SDKInitializer.getService(CrashService::class)?.also { crash ->
                    crash.consented = consent
                }
                _settingUIState.update { uiState ->
                    uiState.copy(consentCrashReportCollection = consent)
                }
            }
            ConsentType.USAGE -> {
                _settingUIState.update { uiState ->
                    uiState.copy(consentUsageCollection = consent)
                }
                updateUsageService(consent)
            }
        }
    }

    private fun updateUsageService(allow: Boolean) {
        SDKInitializer.getService(UsageService::class)?.also { usage ->
            usage.userConsented = allow
            if (allow) {
                if (usage.isUsageServiceStarted()) usage.stopUsageBroker(reset = false)
                usage.startUsageBroker()
            } else {
                if (usage.isUsageServiceStarted()) {
                    usage.stopUsageBroker(reset = true)
                }
            }
        }
    }

    fun updateLogLevel(level: Level) {
        viewModelScope.launch(Dispatchers.IO) {
            logger.debug("update preference as {}", level)
            sharedPreferenceRepository.updateLogLevel(level)
            SDKInitializer.getService(LoggingService::class)?.also { loggingService ->
                val policy = loggingService.policy
                loggingService.policy = policy.copy(logLevel = LogPolicy.getLogLevelString(level))
            }
        }
    }

    fun uploadLog(lifecycleOwner: LifecycleOwner) {
        operationStart()
        SDKInitializer.getService(LoggingService::class)?.also { logging ->
            logging.upload(owner = lifecycleOwner, listener = object : ServiceListener<Boolean> {
                override fun onServiceDone(result: ServiceResult<Boolean>) {
                    when (result) {
                        is ServiceResult.SUCCESS -> {
                            logger.debug("Log is uploaded to the server.")
                            operationFinished(
                                result = OperationResult.OperationSuccess(
                                    getApplication<Application>().getString(
                                        R.string.log_upload_ok
                                    )
                                )
                            )
                        }

                        is ServiceResult.FAILURE -> {
                            val message = getApplication<Application>().getString(
                                R.string.service_result_failure_msg,
                                result.message,
                                result.code.name
                            )
                            logger.debug("Log upload failed with error message $message")
                            operationFinished(result = OperationResult.OperationFail(message))
                        }
                    }
                }
            })
        }
    }

    fun uploadUsageData(lifecycleOwner: LifecycleOwner) {
        logger.debug("start upload usage data")
        operationStart()
        SDKInitializer.getService(UsageService::class)?.also { usageService ->
            usageService.uploadUsageData(
                forceUpload = true,
                owner = lifecycleOwner,
                listener = object :
                    ServiceListener<Boolean> {
                    override fun onServiceDone(result: ServiceResult<Boolean>) {
                        when (result) {
                            is ServiceResult.SUCCESS -> {
                                operationFinished(
                                    result = OperationResult.OperationSuccess(
                                        getApplication<Application>().getString(
                                            R.string.usage_upload_ok
                                        )
                                    )
                            )
                        }
                        is ServiceResult.FAILURE -> {
                            val message = getApplication<Application>().getString(
                                R.string.service_result_failure_msg,
                                result.message,
                                result.code.name
                            )
                            operationFinished(result = OperationResult.OperationFail(message))
                        }
                    }
                }
            })
        }
    }

    fun updateTopics(topicOption: SettingsViewModel.TopicOption) {
        val topics = _settingUIState.value.topics
        topics.forEach {
            if (it.id == topicOption.id) {
                it.state = topicOption.state
            }
        }
        val pushService: PushService = PushService()
        if (topicOption.state) {
            SDKInitializer.getService(pushService.getPushService()::class)?.subscribeTopic(topicOption.id, object : RemoteNotificationClient.CallbackListener {
                override fun onSuccess() {
                    _settingUIState.update { uiState ->
                        uiState.copy(topics = topics)
                    }
                }
                override fun onError(e: Throwable) {
                    logger.error("SubscribeTopicFailed", "###Error: $e", e)
                }
            })
        } else {
            SDKInitializer.getService(pushService.getPushService()::class)?.unsubscribeTopic(topicOption.id, object : RemoteNotificationClient.CallbackListener {
                override fun onSuccess() {
                    _settingUIState.update { uiState ->
                        uiState.copy(topics = topics)
                    }
                }
                override fun onError(e: Throwable) {
                    logger.error("UnsubscribeTopicFailed", "###Error: $e", e)
                }
            })
        }
    }

    data class TopicOption(val id: String, val name: String, var state: Boolean)

    companion object {
        private val logger = LoggerFactory.getLogger(SettingsViewModel::class.java)
    }

}
