package com.tomaszrykala.githubbrowser.compose.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

//    @Singleton
//    @Provides
//    fun providesProductService(): ProductService = ProductService.create()
}