package com.tomaszrykala.githubbrowser.compose.ui

import android.content.Context
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.tomaszrykala.githubbrowser.compose.FindQuery
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.repository.Repository
import com.tomaszrykala.githubbrowser.compose.testutil.mockkR
import com.tomaszrykala.githubbrowser.compose.usecase.OpenRepoUseCase
import com.tomaszrykala.githubbrowser.compose.usecase.SearchReposUseCase
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ReposViewModelTest {

    private val mockSearchReposUseCase: SearchReposUseCase = mockkR()
    private val mockOpenRepoUseCase: OpenRepoUseCase = mockkR()
    private val testScope = TestScope()
    private val search = "google"

    private val sut = ReposViewModel(
        testScope,
        testScope,
        mockSearchReposUseCase,
        mockOpenRepoUseCase
    )

    @Test
    fun `GIVEN empty ReadyState is returned WHEN search THEN return it`() = testScope.runTest {
        val state = RepoState.ReadyState(emptyList())
        coEvery { mockSearchReposUseCase.execute(search) } returns Result.success(state)

        sut.searchRepos(search)
        advanceUntilIdle()

        coVerify { mockSearchReposUseCase.execute(search) }
        assertThat(sut.state.value).isEqualTo(state)
        assertThat(sut.lastSearch).isNull()
    }

    @Test
    fun `GIVEN ReadyState is returned WHEN search THEN return it`() = testScope.runTest {
        val onRepo = FindQuery.OnRepository(name = "name", url = "url", FindQuery.Stargazers(1))
        val repo = Repository(onRepo.stargazers.totalCount, onRepo.name, onRepo.url)
        val state = RepoState.ReadyState(listOf(repo))
        coEvery { mockSearchReposUseCase.execute(search) } returns Result.success(state)

        sut.searchRepos(search)
        advanceUntilIdle()

        coVerify { mockSearchReposUseCase.execute(search) }
        assertThat(sut.state.value).isEqualTo(state)
        assertThat(sut.lastSearch).isNull()
    }

    @Test
    fun `GIVEN ErrorState is returned WHEN search THEN return it and don't clear lastSearch`() =
        testScope.runTest {
            val state = RepoState.ErrorState(emptyList())
            coEvery { mockSearchReposUseCase.execute(search) } returns Result.success(state)

            sut.searchRepos(search)
            advanceUntilIdle()

            coVerify { mockSearchReposUseCase.execute(search) }
            assertThat(sut.state.value).isEqualTo(state)
            assertThat(sut.lastSearch).isEqualTo(search)
        }

    @Test
    fun `GIVEN Exception is thrown WHEN search THEN return it and don't clear lastSearch`() =
        testScope.runTest {
            val exception = IOException("error")
            coEvery { mockSearchReposUseCase.execute(search) } returns Result.failure(exception)

            sut.searchRepos(search)
            advanceUntilIdle()

            coVerify { mockSearchReposUseCase.execute(search) }
            assertThat(sut.state.value).isEqualTo(RepoState.ErrorState(listOf(exception.message!!)))
            assertThat(sut.lastSearch).isEqualTo(search)
        }

//    @Test
//    fun `GIVEN no previous search WHEN onStart THEN do nothing`() {
//        sut.onStart()
//
//        verify { mockSearchReposUseCase wasNot Called }
//        verify { mockOpenRepoUseCase wasNot Called }
//    }

    @Test
    fun `GIVEN no previous search WHEN retrySearch THEN do nothing`() {
        sut.retrySearch()

        verify { mockSearchReposUseCase wasNot Called }
        verify { mockOpenRepoUseCase wasNot Called }
    }

    @Test
    fun `WHEN openRepo THEN call openRepoUseCase`() {
        val mockUri = mockkR<Uri>()
        val mockContext = mockkR<Context>()

        sut.openRepo(mockUri, mockContext)

        verify { mockOpenRepoUseCase.execute(mockUri, mockContext) }
    }
}