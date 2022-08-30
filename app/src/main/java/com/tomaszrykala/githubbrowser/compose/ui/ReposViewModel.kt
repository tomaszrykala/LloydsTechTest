package com.tomaszrykala.githubbrowser.compose.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.tomaszrykala.githubbrowser.compose.di.ApplicationScope
import com.tomaszrykala.githubbrowser.compose.di.MainScope
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.usecase.OpenRepoUseCase
import com.tomaszrykala.githubbrowser.compose.usecase.SearchReposUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ReposViewModel @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    @MainScope private val mainScope: CoroutineScope,
    private val searchReposUseCase: SearchReposUseCase,
    private val openRepoUseCase: OpenRepoUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<RepoState>(RepoState.InitState)
    val state: StateFlow<RepoState> = _state

    private var appScopeJob: Job? = null
    private var _lastSearch: String? = null
    val lastSearch get() = _lastSearch

    fun searchRepos(search: String) {
        if (search.isNotBlank()) {
            _lastSearch = search
            search(search)
        }
    }

    private fun search(search: String) {
        appScopeJob = appScope.launch {
            _state.value = RepoState.LoadingState
            searchReposUseCase.execute(search).let { result ->
                mainScope.launch {
                    result.getOrElse {
                        RepoState.ErrorState(listOf(it.message ?: "Unknown error."))
                    }.let { state ->
                        _state.value = state
                        if (state !is RepoState.ErrorState) {
                            _lastSearch = null
                        }
                    }
                }
            }
        }
    }

    fun onStart() = retrySearch()

    fun onStop() {
        appScopeJob?.let {
            if (it.isActive) {
                it.cancel()
                appScopeJob = null
            }
        }
    }

    fun retrySearch() {
        _lastSearch?.let { search(it) }
    }

    fun openRepo(uri: Uri, context: Context) = openRepoUseCase.execute(uri, context)
}