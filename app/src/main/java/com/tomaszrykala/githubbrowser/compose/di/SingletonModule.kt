package com.tomaszrykala.githubbrowser.compose.di

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.tomaszrykala.githubbrowser.compose.BuildConfig
import com.tomaszrykala.githubbrowser.compose.repository.RepoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    @Provides
    @Singleton
    fun providesRepoRepository(apolloClient: ApolloClient) = RepoRepository(apolloClient)

    @Provides
    @Singleton
    fun providesApolloClient(
        @ApplicationContext context: Context,
    ): ApolloClient = ApolloClient.Builder()
        .serverUrl(serverUrl)
        .okHttpClient(provideOkHttpClient(context))
        .build()

    private fun provideOkHttpClient(context: Context) = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder().method(original.method, original.body)
            builder.addHeader(authHeaderName, authHeaderValue)
            chain.proceed(builder.build())
        }
        .addInterceptor(ChuckerInterceptor.Builder(context).build())
        .build()

    private const val serverUrl = "https://api.github.com/graphql"
    private const val authHeaderName = "Authorization"
    private const val authHeaderValue = "Bearer " + BuildConfig.AUTH_HEADER // expires 18/06/22
}