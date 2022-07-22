package com.kemsky.musicplayer.ui

import androidx.lifecycle.ViewModel
import com.kemsky.musicplayer.data.model.SearchMusicModel
import com.kemsky.musicplayer.data.repository.MusicRepository
import com.kemsky.musicplayer.helper.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MusicRepository) : ViewModel() {

    /**
     * @param term: The URL-encoded text string you want to search for. For example: jack+johnson.
     * @return flow of emitted response from repository
     */
    suspend fun fetchSearchResult(term: String): Flow<Resource<SearchMusicModel>> = flow {
        emit(Resource.Loading())
        val response = repository.searchArtist(term, "song")
        response.body()?.let { model ->
            emit(Resource.Success(model))
        }
    }.catch { throwable ->
        emit(Resource.Error(throwable.localizedMessage))
    }

}