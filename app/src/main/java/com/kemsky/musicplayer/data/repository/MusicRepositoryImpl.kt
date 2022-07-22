package com.kemsky.musicplayer.data.repository

import com.kemsky.musicplayer.data.model.SearchMusicModel
import com.kemsky.musicplayer.data.remote.ApiService
import retrofit2.Response

class MusicRepositoryImpl(
    private val apiService: ApiService
) : MusicRepository {
    override suspend fun searchArtist(term: String, entity: String): Response<SearchMusicModel> = apiService.searchArtist(term, entity)
}