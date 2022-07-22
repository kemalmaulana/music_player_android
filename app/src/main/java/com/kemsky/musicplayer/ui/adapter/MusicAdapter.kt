package com.kemsky.musicplayer.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kemsky.musicplayer.data.model.SearchMusicModel
import com.kemsky.musicplayer.databinding.MusicItemBinding

class MusicAdapter(private val onMusicClick: (Int) -> Unit) :
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    /**
     * Set list with empty list (will added after calling data from API)
     */
    var listMusic : List<SearchMusicModel.Result?>? = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    /**
     * ViewHolder per item in recyclerview
     * @param binding: ViewBinding of item list
     * @param onMusicClick: Lambda that requires item position and returning the Void/Unit type
     * @return ViewHolder of Recyclerview
     */
    inner class MusicViewHolder(private val binding: MusicItemBinding, onMusicClick: (Int) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                onMusicClick(bindingAdapterPosition)
            }
        }

        /**
         * Bind data to list item
         * @param item: Result Model from API
         */
        fun bindMusic(item: SearchMusicModel.Result?) = with(binding) {
            Glide.with(root.context)
                .load(item?.artworkUrl100)
                .centerCrop()
                .into(imgAlbum)
            txtSongName.text = item?.trackName
            txtAlbumName.text = item?.collectionName
            txtArtistName.text = item?.artistName


            if(item?.currentlyPlaying == true) {
                playingIndicator.visibility = View.VISIBLE
            } else {
                playingIndicator.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder =
        MusicViewHolder(
            MusicItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ) {
            onMusicClick(it)
        }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        return holder.bindMusic(listMusic?.get(position))
    }

    override fun getItemCount(): Int = listMusic?.size ?: 0
}