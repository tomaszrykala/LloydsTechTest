package com.tomaszrykala.githubbrowser.compose.repository

data class Repository(
    val totalCount: Int, val name: String, val url: String
)

sealed class RepoState {
    object InitState : RepoState()
    object LoadingState : RepoState()
    data class ReadyState(val repos: List<Repository>) : RepoState()
    data class ErrorState(val errors: List<String>) : RepoState()
}

// Not the solution for a production app, for a showcase should be ok. TODO debug srcSet ?
internal object DebugRepoStateFactory {
    val readyState = RepoState.ReadyState(
        listOf(Repository(1234, "hello world", "www.google.com"))
    )
    val readyEmptyState = RepoState.ReadyState(emptyList())
    val errorState = RepoState.ErrorState(
        listOf(
            "error1",
            "A much longer Error.",
            "An unknown error has occurred. Check your connection."
        )
    )
}