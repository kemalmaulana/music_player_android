package com.kemsky.musicplayer.ui

import android.app.SearchManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.kemsky.musicplayer.R
import com.kemsky.musicplayer.databinding.ActivityMainBinding
import com.kemsky.musicplayer.helper.Resource
import com.kemsky.musicplayer.helper.nextPosition
import com.kemsky.musicplayer.helper.prevPosition
import com.kemsky.musicplayer.ui.adapter.MusicAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var musicAdapter: MusicAdapter
    private var currentPlayingPosition = -1
    private var lastMusicPosition = -1
    private var mediaPlayer: MediaPlayer? = null
    private var musicUrl: String? = null
    private var artworkUrl: String? = null
    private var artistName: String? = null
    private var trackName: String? = null
    private var duration: Int? = null
    private var currentPos: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        initializeAdapter()
        setupRecyclerView()
        initializeUI()
        setupSearchView()
        setupList("a")
    }

    /**
     * Setup List by calling fetchSearchResult from viewModel, and classify the type of Resource class
     */
    private fun setupList(term: String) {
        stopPlayer()
        binding.cardControl.visibility = View.GONE
        lifecycleScope.launchWhenStarted {
            viewModel.fetchSearchResult(term).collectLatest { result ->
                // Handling error first then return the data if there's no error occurs
                when (result) {
                    is Resource.Loading -> {
                        binding.loadingProgress.visibility = View.VISIBLE
                        binding.message.text = "Loading..."
                    }
                    is Resource.Error -> {
                        binding.loadingProgress.visibility = View.GONE
                        binding.message.text = "Something Went Wrong"
                    }
                    else -> {
                        binding.loadingProgress.visibility = View.GONE
                        binding.message.visibility = View.GONE
                        if (::musicAdapter.isInitialized) {
                            musicAdapter.listMusic = result.data?.results
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize Adapter and get value from single ListTile in recyclerview to activity
     */
    private fun initializeAdapter() {
        if (!::musicAdapter.isInitialized) {
            musicAdapter = MusicAdapter {
                currentPlayingPosition = it

                artworkUrl = musicAdapter.listMusic?.get(it)?.artworkUrl60
                artistName = musicAdapter.listMusic?.get(it)?.artistName
                trackName = musicAdapter.listMusic?.get(it)?.trackName
                musicUrl = musicAdapter.listMusic?.get(it)?.previewUrl
                duration = musicAdapter.listMusic?.get(it)?.trackTimeMillis
                settingIndicatorMusic(currentPlayingPosition, true)
                stopPlayer()
                musicUrl?.let { url -> playMusic(url) }

                binding.imgPlayingCover.let { imageView ->
                    Glide.with(this@MainActivity)
                        .load(artworkUrl)
                        .centerCrop()
                        .into(imageView)
                }

                binding.txtPlayingArtist.text = artistName
                binding.txtPlayingTitle.text = trackName

                if (lastMusicPosition != -1 && lastMusicPosition != currentPlayingPosition) {
                    settingIndicatorMusic(lastMusicPosition, false)
                }
                lastMusicPosition = currentPlayingPosition
            }
        }
    }

    /**
     * Setup the recyclerview with the instantiated adapter, layoutmanager, and add divider to recyclerview
     */
    private fun setupRecyclerView() {
        with(binding.rvMusic) {
            adapter = musicAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL
                )
            )
        }
    }

    /**
     * Play music from URL, preparing mediaplayer and updating seekbar
     */
    private fun playMusic(url: String) {
        val uri = Uri.parse(url)
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setDataSource(this@MainActivity.applicationContext, uri)
            prepare()
            start()
        }

        binding.imgPlayPause.setImageResource(R.drawable.ic_baseline_pause_24)
        binding.cardControl.visibility = View.VISIBLE
        updateSeekBar()
    }

    /**
     * Updating Seekbar per second and set on slide from user
     */
    private fun updateSeekBar() {
        binding.seekbar.valueTo = (mediaPlayer?.duration?.div(1000))?.toFloat() ?: 0f

        val handler = Handler(Looper.getMainLooper())
        runOnUiThread(object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    val currentPos = mediaPlayer?.currentPosition?.div(1000)
                    binding.seekbar.value = currentPos?.toFloat() ?: 0f
                }
                handler.postDelayed(this, 1000)
            }
        })

        binding.seekbar.addOnChangeListener { _, value, fromUser ->
            if (mediaPlayer != null && fromUser) {
                mediaPlayer?.seekTo(value.times(1000).toInt())
            }
        }
    }


    private fun stopPlayer() {
        mediaPlayer = if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            null
        } else {
            null
        }
    }

    /**
     * Showing music indicator from the first play
     */
    private fun settingIndicatorMusic(position: Int, isPlay: Boolean) {
        val music = musicAdapter.listMusic?.get(position)
        music?.currentlyPlaying = isPlay
        musicAdapter.notifyItemChanged(position)
    }

    /**
     * Setting up common UIs like Previous, Play, Next buttons
     */
    private fun initializeUI() {
        binding.imgPlayPause.setOnClickListener { _ ->
            if (mediaPlayer?.isPlaying == true) {
                binding.imgPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                mediaPlayer?.pause()
                currentPos = mediaPlayer?.currentPosition
            } else {
                binding.imgPlayPause.setImageResource(R.drawable.ic_baseline_pause_24)
                currentPos?.let { mediaPlayer?.seekTo(it) }
                mediaPlayer?.start()
//                musicUrl?.let { playMusic(it) }
            }
        }


        binding.imgSkipNext.setOnClickListener {
            val music = musicAdapter.listMusic
            val nextPosition = currentPlayingPosition.nextPosition()
            music?.size?.let { musicCount ->
                if (currentPlayingPosition < musicCount) {
                    stopPlayer()
                    settingIndicatorMusic(currentPlayingPosition, false)

                    settingIndicatorMusic(nextPosition, true)
                    music[nextPosition]?.previewUrl?.let { url -> playMusic(url) }
                    currentPlayingPosition = nextPosition
                    musicUrl = musicAdapter.listMusic?.get(nextPosition)?.previewUrl

                }
            }
        }


        binding.imgSkipPrev.setOnClickListener {
            val music = musicAdapter.listMusic
            val prevPosition = currentPlayingPosition.prevPosition()
            if (currentPlayingPosition != 0) {
                stopPlayer()
                settingIndicatorMusic(currentPlayingPosition, false)

                settingIndicatorMusic(prevPosition, true)
                music?.get(prevPosition)?.previewUrl?.let { url -> playMusic(url) }
                currentPlayingPosition = prevPosition
                musicUrl = musicAdapter.listMusic?.get(prevPosition)?.previewUrl
            }
        }
    }

    /**
     * Setup searchview in appbar, setting with on submit.
     */
    private fun setupSearchView() {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = binding.searchView
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query?.isNotEmpty() == true) {
                        currentPlayingPosition = -1
                        lastMusicPosition = -1
                        setupList(query.replace(" ", "+"))
                    }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    /**
     * Stop pause on stop
     */
    override fun onStop() {
        super.onStop()
        stopPlayer()
    }

    /**
     * Stop pause on pause
     */
    override fun onPause() {
        super.onPause()
        stopPlayer()
    }

}