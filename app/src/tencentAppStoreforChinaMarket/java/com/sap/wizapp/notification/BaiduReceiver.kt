package com.sap.wizapp.notification

import android.content.Context
import com.sap.cloud.mobile.foundation.remotenotification.SAPBaiduPushMessageReceiver
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BaiduReceiver : SAPBaiduPushMessageReceiver() {

    override fun onMessage(context: Context?, message: String?, customContentString: String?, p3: Int, p4: Int) {
        super.onMessage(context!!, message!!, customContentString)
    }

    override fun onNotificationArrived(
        context: Context,
        title: String,
        description: String,
        customContentString: String
    ) {
        super.onNotificationArrived(context, title, description, customContentString)
    }

    override fun onNotificationClicked(
        context: Context,
        title: String,
        description: String,
        customContentString: String
    ) {
        super.onNotificationClicked(context, title, description, customContentString)
    }

    override fun onBind(
        context: Context,
        errorCode: Int,
        appId: String,
        userId: String,
        channelId: String,
        requestId: String
    ) {
        super.onBind(context, errorCode, appId, userId, channelId, requestId)
    }

    companion object{
        val logger: Logger = LoggerFactory.getLogger(BaiduReceiver::class.java)
    }
}