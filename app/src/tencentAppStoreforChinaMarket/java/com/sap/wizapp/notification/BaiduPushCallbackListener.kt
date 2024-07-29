package com.sap.wizapp.notification

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import com.sap.wizapp.R
import com.sap.wizapp.ui.AlertDialogComponent
import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler
import com.sap.cloud.mobile.foundation.remotenotification.*

class BaiduPushCallbackListener : PushCallbackListener {

    override fun onReceive(context: Context, message: PushRemoteMessage) {
        AppLifecycleCallbackHandler.getInstance().activity?.let {
            showMessageDialog(it, message)
        }
    }

    private fun showMessageDialog(activity: Activity, message: PushRemoteMessage) {
        activity.addContentView(
            ComposeView(activity).apply {
                setContent {
                    var showDialog by remember { mutableStateOf(true) }
                    if (showDialog) {
                        val textMessage = message.alert ?: activity.getString(R.string.push_text)
                        val notificationTitle = message.title?: activity.getString(R.string.push_message)
                        AlertDialogComponent(title = notificationTitle, text = textMessage, onPositiveButtonClick = {
                            message.notificationID?.let {
                                BasePushService.setPushMessageStatus(it, PushRemoteMessage.NotificationStatus.CONSUMED)
                            }
                            showDialog = false
                        })
                    }
                }
            },
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }
}
