package com.tomaszrykala.githubbrowser.compose

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tomaszrykala.githubbrowser.compose.data.RepoResultDto
import com.tomaszrykala.githubbrowser.compose.di.ApplicationScope
import com.tomaszrykala.githubbrowser.compose.di.MainScope
import com.tomaszrykala.githubbrowser.compose.repository.RepoRepository
import com.tomaszrykala.githubbrowser.compose.repository.ReposState
import com.tomaszrykala.githubbrowser.compose.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReposViewModel @Inject constructor(
    private val repoRepository: RepoRepository,
    @ApplicationScope private val appScope: CoroutineScope,
    @MainScope private val mainScope: CoroutineScope,
) : ViewModel() {

    private val _repositories = MutableLiveData<List<Repository>>()
    val repositories: LiveData<List<Repository>> by lazy { _repositories }

    private val _state = MutableLiveData<ReposState>()
    val state: LiveData<ReposState> by lazy { _state }

    private var appScopeJob: Job? = null

    fun fetchRepos() {
        if (appScopeJob == null || appScopeJob?.isCancelled == true) {

            appScopeJob = appScope.launch {
                println("CSQ GitHub repos : start $appScopeJob")

                repoRepository.query() // TODO add string argument
                    .collectLatest { result ->
                        println("CSQ GitHub result : $result")

                        if (result is RepoResultDto.Success) {
                            mapSuccess(result.data).let {
                                mainScope.launch {
                                    _repositories.value = it
                                    _state.value = ReposState.OkState
                                }
                            }
                        } else {
                            mainScope.launch {
                                _state.value = ReposState.ErrorState(result.toString())
                            }
                        }
                    }

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


    fun onStop() {
        appScopeJob?.let {
            if (it.isActive) {
                it.cancel()
                appScopeJob = null
            }
        }
    }
}