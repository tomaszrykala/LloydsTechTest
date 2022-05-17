package com.tomaszrykala.githubbrowser.compose.data

import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = ""
private const val AUTH_TOKEN = ""

interface RepoService {
    @GET("repos")
    suspend fun getRepos(
        @Query("token") token: String = AUTH_TOKEN,
    ): List<RepoResultDto>

    companion object {
        fun create(): RepoService {
            val logger = LoggingInterceptor.Builder()
                .setLevel(Level.BASIC)
                .log(Platform.INFO)
                .request("Request")
                .response("Response")
                .build()

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(RepoService::class.java)
        }
    }
}