package com.tomaszrykala.githubbrowser.compose.ui

import android.content.Context
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.tomaszrykala.githubbrowser.compose.FindQuery
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.repository.Repository
import com.tomaszrykala.githubbrowser.compose.testutil.MainDispatcherRule
import com.tomaszrykala.githubbrowser.compose.testutil.mockkR
import com.tomaszrykala.githubbrowser.compose.usecase.OpenRepoUseCase
import com.tomaszrykala.githubbrowser.compose.usecase.SearchReposUseCase
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ReposViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockSearchReposUseCase: SearchReposUseCase = mockkR()
    private val mockOpenRepoUseCase: OpenRepoUseCase = mockkR()
    private val search = "google"

    private val sut = ReposViewModel(mockSearchReposUseCase, mockOpenRepoUseCase)

    @Test
    fun `GIVEN empty ReadyState is returned WHEN search THEN return it`() = runTest {
        val state = RepoState.Ready(emptyList())
        coEvery { mockSearchReposUseCase(search) } returns Result.success(state)

        sut.searchRepos(search)
        advanceUntilIdle()

        coVerify { mockSearchReposUseCase(search) }
        assertThat(sut.state.value).isEqualTo(state)
        assertThat(sut.lastSearch).isNull()
    }

    @Test
    fun `GIVEN ReadyState is returned WHEN search THEN return it`() = runTest {
        val onRepo = FindQuery.OnRepository(name = "name", url = "url", FindQuery.Stargazers(1))
        val repo = Repository(onRepo.stargazers.totalCount, onRepo.name, onRepo.url)
        val state = RepoState.Ready(listOf(repo))
        coEvery { mockSearchReposUseCase(search) } returns Result.success(state)

        sut.searchRepos(search)
        advanceUntilIdle()

        coVerify { mockSearchReposUseCase(search) }
        assertThat(sut.state.value).isEqualTo(state)
        assertThat(sut.lastSearch).isNull()
    }

    @Test
    fun `GIVEN ErrorState is returned WHEN search THEN return it and don't clear lastSearch`() =
        runTest {
            val state = RepoState.Error(emptyList())
            coEvery { mockSearchReposUseCase(search) } returns Result.success(state)

            sut.searchRepos(search)
            advanceUntilIdle()

            coVerify { mockSearchReposUseCase(search) }
            assertThat(sut.state.value).isEqualTo(state)
            assertThat(sut.lastSearch).isEqualTo(search)
        }

    @Test
    fun `GIVEN Exception is thrown WHEN search THEN return it and don't clear lastSearch`() =
        runTest {
            val exception = IOException("error")
            coEvery { mockSearchReposUseCase(search) } returns Result.failure(exception)

            sut.searchRepos(search)
            advanceUntilIdle()

            coVerify { mockSearchReposUseCase(search) }
            assertThat(sut.state.value).isEqualTo(RepoState.Error(listOf(exception.message!!)))
            assertThat(sut.lastSearch).isEqualTo(search)
        }

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

        verify { mockOpenRepoUseCase(mockUri, mockContext) }
    }
}