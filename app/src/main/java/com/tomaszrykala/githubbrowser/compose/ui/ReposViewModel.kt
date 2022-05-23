package com.tomaszrykala.githubbrowser.compose

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tomaszrykala.githubbrowser.compose.di.ApplicationScope
import com.tomaszrykala.githubbrowser.compose.di.MainScope
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.usecase.OpenRepoUseCase
import com.tomaszrykala.githubbrowser.compose.usecase.SearchReposUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IReposViewModel {
    fun fetchRepos(search: String)
    fun openRepo(uri: Uri, context: Context)
    fun onStart()
    fun onStop()
}

@HiltViewModel
internal class ReposViewModel @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    @MainScope private val mainScope: CoroutineScope,
    private val searchReposUseCase: SearchReposUseCase,
    private val openRepoUseCase: OpenRepoUseCase,
) : ViewModel(), IReposViewModel {

    private val _state = mutableStateOf<RepoState>(RepoState.InitState)
    val state: State<RepoState> = _state

    private var appScopeJob: Job? = null
    private var lastSearch: String? = null

    override fun fetchRepos(search: String) {
        if (search.isNotBlank()) {
            lastSearch = search
            searchRepos(search)
        }
    }

    private fun searchRepos(search: String) {
        appScopeJob = appScope.launch {
            _state.value = RepoState.LoadingState
            searchReposUseCase.execute(search).let {
                mainScope.launch {
                    _state.value = it.getOrDefault(RepoState.InitState)
                    lastSearch = null
                }
            }
        }
    }

    override fun onStart() {
        lastSearch?.let { searchRepos(it) }
    }

    override fun onStop() {
        appScopeJob?.let {
            if (it.isActive) {
                it.cancel()
                appScopeJob = null
            }
        }
    }

    override fun openRepo(uri: Uri, context: Context) = openRepoUseCase.execute(uri, context)
}