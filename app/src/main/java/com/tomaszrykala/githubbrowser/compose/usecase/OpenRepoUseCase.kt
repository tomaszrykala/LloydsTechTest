package com.tomaszrykala.githubbrowser.compose.usecase

import android.content.Context
import android.net.Uri
import androidx.annotation.MainThread
import com.tomaszrykala.githubbrowser.compose.ui.util.CustomTabsLauncher
import javax.inject.Inject

class OpenRepoUseCase @Inject constructor(
    private val tabsLauncher: CustomTabsLauncher,
) {

    @MainThread
    fun execute(uri: Uri, context: Context) = tabsLauncher.launch(uri, context)
}