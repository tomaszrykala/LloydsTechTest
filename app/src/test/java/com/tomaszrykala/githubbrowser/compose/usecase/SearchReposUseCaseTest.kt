package com.tomaszrykala.githubbrowser.compose.usecase

import com.google.common.truth.Truth.assertThat
import com.tomaszrykala.githubbrowser.compose.FindQuery
import com.tomaszrykala.githubbrowser.compose.dto.RepoResultDto
import com.tomaszrykala.githubbrowser.compose.repository.RepoRepository
import com.tomaszrykala.githubbrowser.compose.repository.RepoState
import com.tomaszrykala.githubbrowser.compose.repository.Repository
import com.tomaszrykala.githubbrowser.compose.testutil.mockkR
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchReposUseCaseTest {

    private val mockRepoRepository: RepoRepository = mockkR()
    private val mockData: FindQuery.Data = mockkR()
    private val mockSearch: FindQuery.Search = mockkR()
    private val search = "search"

    private val sut = SearchReposUseCase(mockRepoRepository)

    @Test
    fun `GIVEN initial sub WHEN execute THEN return success InitState`() =
        runTest(UnconfinedTestDispatcher()) {
            every { mockRepoRepository.queryFlow(search) } returns flow { }

            val result = sut.execute(search)

            assertThat(result).isEqualTo(Result.success(RepoState.InitState))
        }

    @Test
    fun `GIVEN Repos are returned WHEN execute THEN return success ReadyState`() =
        runTest(UnconfinedTestDispatcher()) {
            every { mockData.search } returns mockSearch
            val repoOne = FindQuery.OnRepository(
                name = "name", url = "url", FindQuery.Stargazers(1)
            )
            val repo = Repository(repoOne.stargazers.totalCount, repoOne.name, repoOne.url)
            every { mockSearch.nodes } returns listOf(FindQuery.Node("name", repoOne))

            every { mockRepoRepository.queryFlow(search) } returns flow {
                emit(RepoResultDto.Success(mockData))
            }

            val result = sut.execute(search)

            assertThat(result).isEqualTo(Result.success(RepoState.ReadyState(listOf(repo))))
        }

    @Test
    fun `GIVEN Error is returned WHEN execute THEN return success ErrorState`() =
        runTest(UnconfinedTestDispatcher()) {
            val errors = listOf("error 1", "error 2")
            every { mockRepoRepository.queryFlow(search) } returns flow {
                emit(RepoResultDto.Error(errors))
            }

            val result = sut.execute(search)

            assertThat(result).isEqualTo(Result.success(RepoState.ErrorState(errors)))
        }

    @Test
    fun `GIVEN data is null WHEN mapSuccess THEN return empty list`() {
        val list = sut.mapSuccess(null)

        assertThat(list).isEmpty()
    }

    @Test
    fun `GIVEN nodes is null WHEN mapSuccess THEN return empty list`() {
        every { mockData.search } returns mockSearch
        val list = sut.mapSuccess(mockData)

        assertThat(list).isEmpty()
    }

    @Test
    fun `GIVEN nodes is empty WHEN mapSuccess THEN return empty list`() {
        every { mockData.search } returns mockSearch
        every { mockSearch.nodes } returns emptyList()
        val list = sut.mapSuccess(mockData)

        assertThat(list).isEmpty()
    }

    @Test
    fun `GIVEN nodes is a list of nulls WHEN mapSuccess THEN return empty list`() {
        every { mockData.search } returns mockSearch
        every { mockSearch.nodes } returns listOf(null, null)
        val list = sut.mapSuccess(mockData)

        assertThat(list).isEmpty()
    }

    @Test
    fun `GIVEN data has FindQuery_OnRepositories WHEN mapSuccess THEN return mapped list`() {
        every { mockData.search } returns mockSearch
        val repoOne = FindQuery.OnRepository(
            name = "name", url = "url", FindQuery.Stargazers(1)
        )
        val repoTwo = FindQuery.OnRepository(
            name = "nameTwo", url = "urlTwo", FindQuery.Stargazers(5)
        )
        every { mockSearch.nodes } returns listOf(
            null,
            FindQuery.Node("name", repoOne),
            FindQuery.Node("nameTwo", repoTwo),
            null
        )

        val list = sut.mapSuccess(mockData)

        assertThat(list).hasSize(2)
        assertThat(list[0]).isEqualTo(
            Repository(repoOne.stargazers.totalCount, repoOne.name, repoOne.url)
        )
        assertThat(list[1]).isEqualTo(
            Repository(repoTwo.stargazers.totalCount, repoTwo.name, repoTwo.url)
        )
    }
}