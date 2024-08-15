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
) : ViewModel() {

    private val _state = MutableStateFlow<RepoState>(RepoState.InitState)
    val state: StateFlow<RepoState> = _state

    @VisibleForTesting
    internal var lastSearch: String? = null
        private set

    fun searchRepos(search: String) {
        if (search.isNotBlank()) {
            lastSearch = search
            search(search)
        }
    }

    fun retrySearch() {
        lastSearch?.let { search(it) }
    }

    fun openRepo(uri: Uri, context: Context) {
        openRepoUseCase.execute(uri, context)
    }

    private fun search(search: String) {
        viewModelScope.launch {
            _state.value = RepoState.LoadingState
            withContext(Dispatchers.IO) {
                searchReposUseCase(search).getOrElse {
                    RepoState.ErrorState(listOf(it.message ?: "Unknown error."))
                }.let { state ->
                    withContext(Dispatchers.Main) {
                        _state.value = state
                        if (state !is RepoState.ErrorState) {
                            lastSearch = null
                        }
                    }
                }
            }
        }
    }
}