package com.tomaszrykala.githubbrowser.compose.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tomaszrykala.githubbrowser.compose.ui.theme.LloydsTechTestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ReposViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LloydsTechTestTheme {
                GithubBrowserScreen(viewModel)
            }
        }
    }
}
