package com.sap.wizapp.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import com.sap.wizapp.R
import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler
import com.sap.cloud.mobile.foundation.remotenotification.*

class PushService() {
    fun getPushService(): FirebasePushService {
        return FirebasePushService()
    }

    fun getInitializedPushService(parameters: RemoteNotificationParameters, context: Context, packageManager: PackageManager, packageName: String): FirebasePushService {
        val firebasePushService: FirebasePushService = FirebasePushService()
        firebasePushService.apply {
            setPushCallbackListener(FCMPushCallbackListener())
            setPushServiceConfig(configPushServiceConfig(context))
            isEnableAutoMessageHandling = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                isEnableBadgeMessage = true
                badgeServiceChannel = NotificationChannel("Badge Message Channel",context.getString(
                    R.string.push_badge_channel_name), NotificationManager.IMPORTANCE_HIGH).apply {
                    setShowBadge(true)
                    description = context.getString(R.string.push_badge_channel_description)
                }
            }
            initRemoteNotificationParameters(parameters)
            backgroundNotificationInterceptor = badgeServiceChannel?.let {
                FCMBackgroundNotificationInterceptor(context,
                    it
                )
            }
            addForegroundPushNotificationConditions(object : ForegroundPushNotificationReady {
                override fun onConditionReady(): Boolean {
                    val packageIntent = packageManager.getLaunchIntentForPackage(packageName)
                    val actName = packageIntent!!.resolveActivity(packageManager).className
                    return actName != AppLifecycleCallbackHandler.getInstance().activity!!.javaClass.name
                }
            })
            foregroundNotificationInterceptor = object : BasePushService.ForegroundNotificationInterceptor {
                override fun onForegroundNotificationMessageReceived(pushNotificationEvent: BasePushService.PushNotificationEvent) {
                    pushNotificationEvent.displayNotificationWhen(pushNotificationEvent.pushRemoteMessage, object :
                        ForegroundPushNotificationReady {
                        override fun onConditionReady(): Boolean {
                            return AppLifecycleCallbackHandler.getInstance().activity?.let {
                                it.javaClass.name != "com.sap.wizapp.app.WelcomeActivity"
                            } ?: false
                        }
                    })
                }
            }
        }
        return firebasePushService
    }

    private fun configPushServiceConfig(context: Context): PushServiceConfig {
        val notificationConfig = NotificationConfig(
            smallIcon = R.mipmap.ic_statusbar,
            largeIcon = R.mipmap.ic_launcher,
            notificationTime = 0,
            isAutoCancel = true,
            badgeIconType = Notification.BADGE_ICON_SMALL,
            cancelButtonDescription = context.resources.getString(R.string.cancel),
            cancelButtonIcon = R.drawable.ic_sap_icon_decline)
        val notificationChannelConfig = NotificationChannelConfig(
            channelID = context.getString(R.string.push_channel_id),
            description = "Notification channel for push messages",
            channelName = context.getString(R.string.push_channel_name),
            importance = NotificationManager.IMPORTANCE_HIGH,
            enableLights = true,
            lightColor = Color.RED,
            enableVibration = true,
            vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400),
            enableBadge = false

        )
        return PushServiceConfig(notificationChannelConfig ,notificationConfig)
    }
}