package com.kemsky.musicplayer.data.repository

import com.kemsky.musicplayer.data.model.SearchMusicModel
import retrofit2.Response

interface MusicRepository {

    suspend fun searchArtist(term: String, entity: String): Response<SearchMusicModel>

}