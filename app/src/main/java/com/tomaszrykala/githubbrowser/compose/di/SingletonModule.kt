package com.tomaszrykala.githubbrowser.compose.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.tomaszrykala.githubbrowser.compose.repository.RepoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun providesApolloClient(): ApolloClient = ApolloClient.Builder()
        .serverUrl("https://api.github.com/graphql")
        .okHttpClient(provideOkHttpClient())
        .build()

    private fun provideOkHttpClient() = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder().method(original.method, original.body)
            // expires 18/06/22
            builder.addHeader(
                "Authorization",
                "Bearer " + "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"
            )
            chain.proceed(builder.build())
        }
        .build()
}