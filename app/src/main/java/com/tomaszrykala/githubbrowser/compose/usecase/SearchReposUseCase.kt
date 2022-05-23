package com.tomaszrykala.githubbrowser.compose.usecase

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.tomaszrykala.githubbrowser.compose.FindQuery
import com.tomaszrykala.githubbrowser.compose.dto.RepoResultDto
import com.tomaszrykala.githubbrowser.compose.repository.RepoRepository
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.repository.Repository
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

class SearchReposUseCase @Inject constructor(
    private val repoRepository: RepoRepository,
) {

    suspend fun execute(search: String): Result<RepoState> {
        var state: RepoState = RepoState.InitState
        return repoRepository.queryFlow(search)
            .collectLatest { result ->
                if (result is RepoResultDto.Success) {
                    mapSuccess(result.data).let {
                        state = RepoState.ReadyState(it)
                    }
                } else {
                    state = RepoState.ErrorState(result.toString())
                }
            }
            .runCatching { state }
    }

    @WorkerThread
    @VisibleForTesting
    internal fun mapSuccess(nodes: FindQuery.Data?): List<Repository> {
        return nodes?.search?.nodes
            ?.filterNotNull()
            ?.filter { it.onRepository is FindQuery.OnRepository }
            ?.map { it.onRepository as FindQuery.OnRepository }
            ?.map { Repository(it.stargazers.totalCount, it.name, it.url.toString()) }
            ?: emptyList()
    }
}