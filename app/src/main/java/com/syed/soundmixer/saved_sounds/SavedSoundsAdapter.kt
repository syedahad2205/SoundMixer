package com.syed.soundmixer.saved_sounds

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syed.soundmixer.R
import com.syed.soundmixer.room.SavedSound

class SavedSoundsAdapter(
    private val context: Context,
    private val downloadedSounds: List<SavedSound>,
    private val onClick: (SavedSound) -> Unit,
    private val onLongClick: (SavedSound) -> Unit
) : RecyclerView.Adapter<SavedSoundsAdapter.ViewHolder>() {

    private var selectedFiles = setOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_saved_sound, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val savedSound = downloadedSounds[position]
        holder.fileNameTextView.text = savedSound.fileName

        if (selectedFiles.contains(savedSound.filePath)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#666BCA"))
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#888ef9"))
        }

        holder.itemView.setOnClickListener {
            onClick.invoke(savedSound)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick.invoke(savedSound)
            true
        }
    }

    override fun getItemCount(): Int = downloadedSounds.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateSelectedFiles(selectedFiles: Set<String>) {
        this.selectedFiles = selectedFiles
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)
    }
}

