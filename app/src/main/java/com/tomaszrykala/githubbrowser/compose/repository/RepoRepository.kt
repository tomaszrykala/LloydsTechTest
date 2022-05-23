package com.tomaszrykala.githubbrowser.compose.repository

import androidx.annotation.WorkerThread
import com.apollographql.apollo3.ApolloClient
import com.tomaszrykala.githubbrowser.compose.FindQuery
import com.tomaszrykala.githubbrowser.compose.dto.RepoResultDto
import kotlinx.coroutines.flow.*
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
                emit(RepoResultDto.Error(listOf("An unknown error occurred. Check your connection.")))
            }
}
