package com.kemsky.musicplayer.di

import com.kemsky.musicplayer.data.remote.ApiService
import com.kemsky.musicplayer.data.repository.MusicRepository
import com.kemsky.musicplayer.data.repository.MusicRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Providing ApiService class to Repository via Dagger Hilt
     */
    @Singleton
    @Provides
    fun provideApiService(): ApiService = ApiService()

    /**
     * Providing Repository class to ViewModel via Dagger Hilt
     */
    @Singleton
    @Provides
    fun provideRepository(apiService: ApiService): MusicRepository = MusicRepositoryImpl(apiService)

}