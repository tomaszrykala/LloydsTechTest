package com.tomaszrykala.githubbrowser.compose.repository

data class Repository(
    val totalCount: Int, val name: String, val url: String
)

sealed class RepoState {
    object InitState : RepoState()
    object LoadingState : RepoState()
    data class ReadyState(val repos: List<Repository>) : RepoState()
    data class ErrorState(val error: String) : RepoState()
}