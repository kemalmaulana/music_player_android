package com.kemsky.musicplayer.data.repository

import com.google.gson.Gson
import com.kemsky.musicplayer.data.model.SearchMusicModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class RepositoryTest {

    @Mock
    private lateinit var gson: Gson

    @Mock
    private lateinit var repository: MusicRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `repository post malone test`() = runTest(UnconfinedTestDispatcher()) {
        val songs: MutableList<SearchMusicModel.Result> = mutableListOf()
        val response = SearchMusicModel(resultCount = 50, songs)
        val term = "post+malone"

        // Test from API
        Mockito.lenient().`when`(repository.searchArtist(term, "song"))
            .thenReturn(Response.success(response))

        // Test from model by gson
        Mockito.lenient().`when`(
            gson.fromJson(
                "",
                SearchMusicModel::class.java
            )
        ).thenReturn(response)

        // Interaction test, checking if searchArtist getting called or not
        repository.searchArtist(term, "song")
        verify(repository).searchArtist(term, "song")

        // Test from API but with assert equals to compare returned value from repo and fake response
        Assert.assertEquals(repository.searchArtist(term, "song").body(), response)
    }
}