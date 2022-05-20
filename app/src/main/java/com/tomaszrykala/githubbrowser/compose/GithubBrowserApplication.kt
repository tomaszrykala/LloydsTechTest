package com.tomaszrykala.githubbrowser.compose

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GithubBrowserApplication : Application()

const val TAG = "GithubBrowserApplication"