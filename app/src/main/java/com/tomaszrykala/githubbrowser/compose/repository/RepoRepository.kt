package com.tomaszrykala.githubbrowser.compose.repository

import androidx.annotation.WorkerThread
import com.apollographql.apollo3.ApolloClient
import com.tomaszrykala.githubbrowser.compose.FindQuery
import com.tomaszrykala.githubbrowser.compose.data.RepoResultDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepoRepository @Inject constructor(private val client: ApolloClient) {

    @WorkerThread
    fun query(query: String = "google"): Flow<RepoResultDto> =
        client.query(FindQuery(query = query))
            .toFlow()
            .map {
                if (it.hasErrors()) {
                    RepoResultDto.Error(requireNotNull(it.errors).map { error -> error.message })
                } else {
                    RepoResultDto.Success(it.data)
                }
            }
}
