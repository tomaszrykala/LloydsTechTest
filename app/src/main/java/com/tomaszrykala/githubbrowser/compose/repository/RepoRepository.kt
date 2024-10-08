package com.tomaszrykala.githubbrowser.compose.repository

import androidx.annotation.WorkerThread
import com.apollographql.apollo3.ApolloClient
import com.tomaszrykala.githubbrowser.compose.FindQuery
import com.tomaszrykala.githubbrowser.compose.dto.RepoResultDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepoRepository @Inject constructor(private val client: ApolloClient) {

    @WorkerThread
    fun queryFlow(query: String): Flow<RepoResultDto> =
        client.query(FindQuery("user:$query sort:stars"))
            .toFlow()
            .map {
                if (it.hasErrors()) {
                    RepoResultDto.Error(requireNotNull(it.errors).map { error -> error.message })
                } else {
                    RepoResultDto.Success(it.dataAssertNoErrors)
                }
            }
            .catch {
                emit(RepoResultDto.Error(listOf(ERROR_GENERIC)))
            }

    internal companion object {
        const val ERROR_GENERIC = "An unknown error occurred. Check your connection."
    }
}
