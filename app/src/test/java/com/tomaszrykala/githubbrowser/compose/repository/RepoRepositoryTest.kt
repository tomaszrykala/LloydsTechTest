package com.tomaszrykala.githubbrowser.compose.repository

import app.cash.turbine.test
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Error
import com.benasher44.uuid.Uuid
import com.google.common.truth.Truth.assertThat
import com.tomaszrykala.githubbrowser.compose.FindQuery
import com.tomaszrykala.githubbrowser.compose.dto.RepoResultDto
import com.tomaszrykala.githubbrowser.compose.repository.RepoRepository.Companion.ERROR_GENERIC
import com.tomaszrykala.githubbrowser.compose.testutil.mockkR
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RepoRepositoryTest {

    private val mockApolloClient: ApolloClient = mockkR()
    private val mockCall: ApolloCall<FindQuery.Data> = mockkR()
    private val mockData = mockkR<FindQuery.Data>()
    private val search = "google"

    private val sut = RepoRepository(mockApolloClient)

    @Test
    fun `GIVEN search is successful WHEN queryFlow THEN return Success`() = runTest {
        val findQuery: FindQuery = createFindQuery()
        val response = ApolloResponse
            .Builder(findQuery, Uuid(1L, 1L), mockData)
            .build()
        every { mockApolloClient.query(findQuery) } returns mockCall
        every { mockCall.toFlow() } returns flow { emit(response) }

        val queryFlow = sut.queryFlow(search)

        queryFlow.test {
            assertThat(awaitItem()).isEqualTo(RepoResultDto.Success(mockData))
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN search has errors WHEN queryFlow THEN return the Error`() = runTest {
        val findQuery: FindQuery = createFindQuery()
        val error = mockkR<Error>()
        every { error.message } returns "error 1"
        val response = ApolloResponse
            .Builder(findQuery, Uuid(1L, 1L), mockData)
            .errors(listOf(error))
            .build()
        every { mockApolloClient.query(findQuery) } returns mockCall
        every { mockCall.toFlow() } returns flow { emit(response) }

        val queryFlow = sut.queryFlow(search)

        queryFlow.test {
            assertThat(awaitItem()).isEqualTo(RepoResultDto.Error(listOf(error.message)))
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN search throws WHEN queryFlow THEN return Generic Error`() = runTest {
        val findQuery: FindQuery = createFindQuery()
        every { mockApolloClient.query(findQuery) } returns mockCall
        every { mockCall.toFlow() } returns flow { throw IOException("") }

        val queryFlow = sut.queryFlow(search)

        queryFlow.test {
            assertThat(awaitItem()).isEqualTo(RepoResultDto.Error(listOf(ERROR_GENERIC)))
            awaitComplete()
        }
    }

    private fun createFindQuery() = FindQuery("user:$search sort:stars")
}