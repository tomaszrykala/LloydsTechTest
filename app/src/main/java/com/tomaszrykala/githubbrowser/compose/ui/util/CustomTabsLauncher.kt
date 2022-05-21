package com.tomaszrykala.githubbrowser.compose.ui.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.COLOR_SCHEME_SYSTEM
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_ON
import com.tomaszrykala.githubbrowser.compose.R
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback
import javax.inject.Inject

class CustomTabsLauncher @Inject constructor() {

    fun launch(uri: Uri, context: Context) {
        val intent = CustomTabsIntent.Builder()
            .setShareState(SHARE_STATE_ON)
            .setColorScheme(COLOR_SCHEME_SYSTEM)
            .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
            .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
            .setShowTitle(true)
            .build()

        CustomTabsHelper.addKeepAliveExtra(context, intent.intent)
        CustomTabsHelper.openCustomTab(context, intent, uri, WebViewFallback())
    }
}