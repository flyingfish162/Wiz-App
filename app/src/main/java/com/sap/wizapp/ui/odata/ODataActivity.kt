package com.sap.wizapp.ui.odata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sap.cloud.mobile.flows.compose.ui.FlowComposeTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import com.sap.cloud.mobile.foundation.app.security.ClipboardProtectionService
import com.sap.cloud.mobile.foundation.mobileservices.SDKInitializer

class ODataActivity : ComponentActivity() {
    private val clipboardProtectionService =
        SDKInitializer.getService(ClipboardProtectionService::class)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlowComposeTheme {
                val localClipboardManager =
                    clipboardProtectionService?.let {
                        it.sdkClipboardManager as ClipboardManager
                    } ?: LocalClipboardManager.current
                CompositionLocalProvider(
                    LocalClipboardManager provides localClipboardManager
                ) {
                    val windowSize = calculateWindowSizeClass(this)
                    ODataApp(windowSize = windowSize)
                }
            }
        }
    }
}

@Composable
fun ODataApp(modifier: Modifier = Modifier, windowSize: WindowSizeClass) {
    val navController = rememberNavController()
    ODataNavHost(modifier = modifier, navController = navController, windowSize = windowSize)
}
