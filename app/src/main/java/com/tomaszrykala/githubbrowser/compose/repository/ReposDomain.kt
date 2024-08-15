package com.tomaszrykala.githubbrowser.compose.repository

data class Repository(
    val totalCount: Int, val name: String, val url: String
)

sealed class RepoState {
    object Init : RepoState()
    object Loading : RepoState()
    data class Ready(val repos: List<Repository>) : RepoState()
    data class Error(val errors: List<String>) : RepoState()
}