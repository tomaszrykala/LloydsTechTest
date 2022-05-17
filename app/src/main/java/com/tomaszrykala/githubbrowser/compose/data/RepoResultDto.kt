package com.tomaszrykala.githubbrowser.compose.data

import com.tomaszrykala.githubbrowser.compose.FindQuery

sealed class RepoResultDto {
    data class Success(val data: FindQuery.Data?) : RepoResultDto()
    data class Error(val errors: List<String>) : RepoResultDto()
}