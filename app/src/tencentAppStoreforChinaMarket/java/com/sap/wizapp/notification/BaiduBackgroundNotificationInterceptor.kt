package com.sap.wizapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sap.wizapp.R
import com.sap.cloud.mobile.foundation.remotenotification.*

open class BaiduBackgroundNotificationInterceptor(context: Context, badgeServiceChannel: NotificationChannel) : BasePushService.BackgroundNotificationInterceptor {
    lateinit var notificationPreferences: SharedPreferences
    private val mBadgeServiceChannel = badgeServiceChannel
    val mContext = context
    override fun onBackgroundNotificationMessageReceived(pushNotificationEvent: BasePushService.PushNotificationEvent) {
        notificationPreferences = mContext.getSharedPreferences("NOTIFICATION_CANCELLATION", Context.MODE_PRIVATE)
        // separate the notification into two independent notifications if payload contains both badge and data
        var isBadgeOnlyMessage = false
        if (pushNotificationEvent.pushRemoteMessage!!.isBadgeMessage) {
            val messageData = pushNotificationEvent.pushRemoteMessage!!.data
            if (messageData.size == 2 && messageData.containsKey("badge") && messageData.containsKey("mobileservices.notificationId")) {
                isBadgeOnlyMessage = true
            } else if (messageData.size == 3 && messageData.containsKey("badge") && messageData.containsKey("mobileservices.notificationId") && messageData.containsKey("alert")) {
                isBadgeOnlyMessage = true
            }
        }
        val badgeNumber = pushNotificationEvent.pushRemoteMessage!!.badge
        val mesgAlert = pushNotificationEvent.pushRemoteMessage!!.alert
        if (isBadgeOnlyMessage) {
            pushNotificationEvent.pushRemoteMessage!!.alert = mesgAlert?: "$badgeNumber messages received"
            pushNotificationEvent.displayNotification(pushNotificationEvent.pushRemoteMessage)
        } else if (!isBadgeOnlyMessage && pushNotificationEvent.pushRemoteMessage!!.isBadgeMessage) {
            // update the notification as a data only notification
            val dRemoteMessage = pushNotificationEvent.pushRemoteMessage
            dRemoteMessage!!.isBadgeMessage = false
            dRemoteMessage.badge = 0
            dRemoteMessage.data.remove("badge")
            // handle notification cancel, store the businessId and androidNotificationID pairs
            // retrieve the androidNotificationID by pairs when user wants to cancel the notification
            if (dRemoteMessage.data.containsKey("businessId")) {
                storeNotificationInfo(dRemoteMessage)
                pushNotificationEvent.displayNotification(dRemoteMessage)
            } else if (dRemoteMessage.data.containsKey("businessIdOfRemovingNotification")) {
                cancelNotification(dRemoteMessage)
            } else {
                pushNotificationEvent.displayNotification(dRemoteMessage)
            }

            // update the notification as a badge only notification
            val badgeMessage = pushNotificationEvent.pushRemoteMessage
            badgeMessage!!.badge = badgeNumber
            badgeMessage.alert = mesgAlert?: "$badgeNumber messages received"
            badgeMessage.data.clear()
            badgeMessage.data["badge"] = badgeNumber.toString()
            badgeMessage.data["mobileservices.notificationId"] = badgeMessage.notificationID!!
            val notificationManager = NotificationManagerCompat.from(mContext)
            val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBadgeServiceChannel.id
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            val notificationTrampolineActivityIntent =
                Intent(mContext, NotificationTrampolineActivity::class.java)
            notificationTrampolineActivityIntent.run {
                putExtra(
                    BasePushService.NOTIFICATION_ID_FROM_SERVER,
                    badgeMessage.notificationID
                )
                putExtra(
                    BasePushService.NOTIFICATION_TITLE_FROM_SERVER,
                    badgeMessage.title
                )
                putExtra(
                    BasePushService.NOTIFICATION_ALERT_FROM_SERVER,
                    badgeMessage.alert
                )
                putExtra(
                    BasePushService.NOTIFICATION_DATA_FROM_SERVER,
                    badgeMessage.data
                )
                putExtra(BasePushService.NOTIFICATION_CONDITION,pushNotificationEvent.foregroundPushNotificationReady)
            }
            val restartAppWithActivityPendingIntent = PendingIntent.getActivity(
                mContext,
                badgeMessage.androidNotificationID,
                notificationTrampolineActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val builder = channelId?.let {
                NotificationCompat.Builder(mContext,
                    it
                ).setNumber(badgeMessage.badge)
                    .setContentText(badgeMessage.alert)
                    .setTicker(badgeMessage.title)
                    .setContentIntent(restartAppWithActivityPendingIntent)
                    .setSmallIcon(R.drawable.ic_sap_icon_ui_notifications)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setAutoCancel(true)
                    .setDeleteIntent(
                        createOnDismissedIntent(
                            mContext,
                            badgeMessage.androidNotificationID,
                            badgeMessage
                        )
                    )
            }
            if (badgeMessage.extender != null) {
                builder?.extend(badgeMessage.extender as NotificationCompat.Extender)
            }
            builder?.build()?.let { notificationManager.notify( -1, it) }
        } else {
            val dataMessage = pushNotificationEvent.pushRemoteMessage
            val businessId = dataMessage!!.data.get("businessId")
            if (businessId != null) {
                if (dataMessage.data.get("action") == "Create") {
                    storeNotificationInfo(dataMessage)
                    pushNotificationEvent.displayNotification(dataMessage)
                } else if (dataMessage.data.get("action") == "Cancel") {
                    cancelNotification(dataMessage)
                }
            } else {
                pushNotificationEvent.displayNotification(dataMessage)
            }
        }
    }

    private fun createOnDismissedIntent(
        context: Context,
        notificationId: Int,
        pushRemoteMessage: PushRemoteMessage?
    ): PendingIntent? {
        val intent = Intent(context, NotificationDismissedReceiver::class.java)
        intent.putExtra(BasePushService.NOTIFICATION_ID, notificationId)
        intent.putExtra(BasePushService.NOTIFICATION_ID_FROM_SERVER, pushRemoteMessage?.notificationID)
        return PendingIntent.getBroadcast(
            context.applicationContext,
            notificationId, intent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun storeNotificationInfo(message: PushRemoteMessage) {
        val businessId = message.data.get("businessId")
        val notificationCancellationEditor = notificationPreferences.edit()
        notificationCancellationEditor.putString(businessId, message.androidNotificationID.toString())
        notificationCancellationEditor.apply()
        notificationCancellationEditor.commit()
    }

    private fun cancelNotification(message: PushRemoteMessage) {
        val notificationManager: NotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val cancellationEditor = notificationPreferences.edit()
        val businessId = message.data.get("businessId")
        val rNotificationId = notificationPreferences.getString(businessId, null)
        rNotificationId?.let { it.toInt()
            .let { it1 -> notificationManager.cancel(it1) } }
        cancellationEditor.remove(businessId)
        cancellationEditor.apply()
        cancellationEditor.commit()
    }
}