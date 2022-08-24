package com.tomaszrykala.githubbrowser.compose.ui.util

import android.content.Context
import android.net.Uri
import androidx.annotation.MainThread
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.COLOR_SCHEME_SYSTEM
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_ON
import androidx.compose.ui.graphics.toArgb
import com.tomaszrykala.githubbrowser.compose.R
import com.tomaszrykala.githubbrowser.compose.ui.theme.primaryVariant
import javax.inject.Inject

class CustomTabsLauncher @Inject constructor() {

    @MainThread
    fun launch(uri: Uri, context: Context) {
        CustomTabsIntent.Builder()
            .setShareState(SHARE_STATE_ON)
            .setColorScheme(COLOR_SCHEME_SYSTEM)
            .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
            .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(primaryVariant.toArgb())
                    .build()
            )
            .setShowTitle(true)
            .build()
            .apply {
                intent.setPackage(packageName)
                launchUrl(context, uri)
            }
    }

    private companion object {
        const val packageName = "com.android.chrome"
    }
}