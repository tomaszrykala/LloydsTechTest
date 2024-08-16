package com.tomaszrykala.githubbrowser.compose.ui

import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.usecase.OpenRepoUseCase
import com.tomaszrykala.githubbrowser.compose.usecase.SearchReposUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReposViewModel @Inject constructor(
    private val searchReposUseCase: SearchReposUseCase,
    private val openRepoUseCase: OpenRepoUseCase,
) : ViewModel(), GithubReposViewModel {

    private val _state = MutableStateFlow<RepoState>(RepoState.Init)
    override val state: StateFlow<RepoState> = _state

    @VisibleForTesting
    internal var lastSearch: String? = null
        private set

    override fun searchRepos(search: String) {
        if (search.isNotBlank()) {
            lastSearch = search
            search(search)
        }
    }

    override fun retrySearch() {
        lastSearch?.let { search(it) }
    }

    override fun openRepo(uri: Uri, context: Context) {
        openRepoUseCase(uri, context)
    }

    private fun search(search: String) {
        _state.value = RepoState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            searchReposUseCase(search).getOrElse {
                RepoState.Error(listOf(it.message ?: "Unknown error."))
            }.let { state ->
                withContext(Dispatchers.Main) {
                    _state.value = state
                    if (state !is RepoState.Error) {
                        lastSearch = null
                    }
                }
            }
        }
    }
}

interface GithubReposViewModel {
    val state: StateFlow<RepoState>
    fun openRepo(uri: Uri, context: Context)
    fun searchRepos(search: String)
    fun retrySearch()
}