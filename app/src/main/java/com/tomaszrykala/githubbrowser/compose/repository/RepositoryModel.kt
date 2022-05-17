package com.tomaszrykala.githubbrowser.compose.repository

data class Repository(
    val totalCount: Int, val name: String, val url: String
)

sealed class ReposState {
    object OkState : ReposState()
    data class ErrorState(val error: String) : ReposState()
}