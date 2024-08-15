package com.tomaszrykala.githubbrowser.compose.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomaszrykala.githubbrowser.compose.TAG
import com.tomaszrykala.githubbrowser.compose.di.ApplicationScope
import com.tomaszrykala.githubbrowser.compose.di.MainScope
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.usecase.OpenRepoUseCase
import com.tomaszrykala.githubbrowser.compose.usecase.SearchReposUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
class ReposViewModel @Inject constructor(
//    @ApplicationScope private val appScope: CoroutineScope,
//    @MainScope private val mainScope: CoroutineScope,
    private val searchReposUseCase: SearchReposUseCase,
    private val openRepoUseCase: OpenRepoUseCase,
) : ViewModel(), RepoController {

    private val _state = MutableStateFlow<RepoState>(RepoState.InitState)
    val state: StateFlow<RepoState> = _state

    //    private var appScopeJob: Job? = null
    private var _lastSearch: String? = null
    val lastSearch get() = _lastSearch

    override fun searchRepos(search: String) {
        if (search.isNotBlank()) {
            _lastSearch = search
            search(search)
        }
    }

    override fun retrySearch() {
        _lastSearch?.let { search(it) }
    }

    override fun openRepo(uri: Uri, context: Context) {
        Log.d(TAG, "Open repo: $uri.")
        openRepoUseCase.execute(uri, context)
    }

    private fun search(search: String) {
//        appScopeJob = appScope.launch {
        viewModelScope.launch {
            _state.value = RepoState.LoadingState
            withContext(Dispatchers.IO) {
                searchReposUseCase.execute(search).let { result ->
//                mainScope.launch {
                    result.getOrElse {
                        RepoState.ErrorState(listOf(it.message ?: "Unknown error."))
                    }.let { state ->
                        _state.value = state
                        if (state !is RepoState.ErrorState) {
                            _lastSearch = null
                        }
                    }
                }
//                }
            }
        }
    }

//    fun onStart() = retrySearch()
//
//    fun onStop() {
//        appScopeJob?.let {
//            if (it.isActive) {
//                it.cancel()
//                appScopeJob = null
//            }
//        }
//    }
}