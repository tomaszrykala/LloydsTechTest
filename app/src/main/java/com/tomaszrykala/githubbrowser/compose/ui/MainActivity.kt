package com.tomaszrykala.githubbrowser.compose.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.ui.theme.GithubBrowserComposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), RepoController {

    private val viewModel: ReposViewModel by viewModels()
    private val composer = MainActivityComposer()

    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {
        val state: RepoState by viewModel.state

        setContent {
            GithubBrowserComposeTheme {
                composer.GithubBrowser(state, this)
            }
        }
    }

    override fun onStart() = super.onStart().also {
        viewModel.onStart()
    }

    override fun onStop() = super.onStop().also {
        viewModel.onStop()
    }

    override fun fetchRepos(search: String) {
        viewModel.fetchRepos(search)
    }

    override fun openRepo(uri: Uri) {
        viewModel.openRepo(uri, this)
    }
}

interface RepoController {
    fun fetchRepos(search: String)
    fun openRepo(uri: Uri)
}
