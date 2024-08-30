package com.syed.soundmixer.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.syed.soundmixer.R
import com.syed.soundmixer.databinding.ItemSoundBinding
import com.syed.soundmixer.models.Sound
import com.syed.soundmixer.sound.SoundPlayer


class SearchAdapter(
    private val soundPlayer: SoundPlayer,
    private val onDownloadClicked: (Sound) -> Unit
) : RecyclerView.Adapter<SearchAdapter.SoundViewHolder>() {

    private val sounds = mutableListOf<Sound>()
    private var currentlyPlayingPosition: Int? = null

    init {
        soundPlayer.onPlaybackComplete = {
            val previousPosition = currentlyPlayingPosition
            currentlyPlayingPosition = null
            if (previousPosition != null) {
                notifyItemChanged(previousPosition, false)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<Sound>) {
        sounds.clear()
        sounds.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val binding = ItemSoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SoundViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: SoundViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val sound = sounds[position]
        val isPlaying = position == currentlyPlayingPosition
        holder.bind(sound, isPlaying)
    }

    override fun onBindViewHolder(
        holder: SoundViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val isPlaying = payloads[0] as Boolean
            holder.updatePlayButton(isPlaying)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int = sounds.size

    inner class SoundViewHolder(val binding: ItemSoundBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.playButton.setOnClickListener {
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener

                val sound = sounds[position]
                val isPlaying = position == currentlyPlayingPosition

                if (isPlaying) {
                    currentlyPlayingPosition = null
                    soundPlayer.stopSound()
                } else {
                    val previousPosition = currentlyPlayingPosition
                    currentlyPlayingPosition = position
                    notifyItemChanged(previousPosition ?: -1, false)
                    soundPlayer.playSound(sound.previews.previewUrl)
                    notifyItemChanged(position, true)
                }
            }
            binding.downloadButton.setOnClickListener {
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener

                val sound = sounds[position]
                onDownloadClicked(sound)
            }
        }

        fun bind(sound: Sound, isPlaying: Boolean) {
            binding.soundTitle.text = sound.name
            updatePlayButton(isPlaying)
        }

        fun updatePlayButton(isPlaying: Boolean) {
            if (isPlaying) {
                binding.playButton.setImageResource(R.drawable.ic_stop)
            } else {
                binding.playButton.setImageResource(R.drawable.ic_play_white)
            }
        }
    }
}
