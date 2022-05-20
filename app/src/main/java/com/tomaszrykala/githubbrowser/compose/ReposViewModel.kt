package com.tomaszrykala.githubbrowser.compose

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tomaszrykala.githubbrowser.compose.dto.RepoResultDto
import com.tomaszrykala.githubbrowser.compose.di.ApplicationScope
import com.tomaszrykala.githubbrowser.compose.di.MainScope
import com.tomaszrykala.githubbrowser.compose.repository.RepoRepository
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IReposViewModel {
    fun fetchRepos(search: String)
    fun onStart()
    fun onStop()
}

@HiltViewModel
internal class ReposViewModel @Inject constructor(
    private val repoRepository: RepoRepository,
    @ApplicationScope private val appScope: CoroutineScope,
    @MainScope private val mainScope: CoroutineScope,
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
            Log.d(TAG, "Repos load start: $appScopeJob")

            repoRepository.queryFlow(search)
                .collectLatest { result ->
                    Log.d(TAG, "Repos result: $result")

                    if (result is RepoResultDto.Success) {
                        mapSuccess(result.data).let {
                            mainScope.launch {
                                _state.value = RepoState.ReadyState(it)
                            }
                        }
                    } else {
                        mainScope.launch {
                            _state.value = RepoState.ErrorState(result.toString())
                        }
                    }
                    lastSearch = null
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

    @WorkerThread
    private fun mapSuccess(nodes: FindQuery.Data?): List<Repository> {
        return nodes?.search?.nodes
            ?.filterNotNull()
            ?.filter { it.onRepository is FindQuery.OnRepository }
            ?.map { it.onRepository as FindQuery.OnRepository }
            ?.map { Repository(it.stargazers.totalCount, it.name, it.url.toString()) }
            ?: emptyList()
    }
}