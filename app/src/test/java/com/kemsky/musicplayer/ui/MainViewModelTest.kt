package com.kemsky.musicplayer.ui

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kemsky.musicplayer.data.repository.MusicRepository
import com.kemsky.musicplayer.helper.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @Mock
    private lateinit var repository: MusicRepository

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = MainViewModel(repository)
    }

    @Test
    fun `flow test from viewmodel`() = runBlocking {
        val term = "post+malone"
        viewModel.fetchSearchResult(term).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            assertThat(awaitItem()).isInstanceOf(Resource.Error::class.java)
            cancelAndConsumeRemainingEvents()
        }
    }

}