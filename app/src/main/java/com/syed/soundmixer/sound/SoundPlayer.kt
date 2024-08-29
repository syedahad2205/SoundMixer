package com.syed.soundmixer.sound

import android.media.MediaPlayer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
class SoundPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null
    var onPlaybackComplete: (() -> Unit)? = null


    fun playSound(url: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else {
            mediaPlayer?.reset()
        }

        try {
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener {
                it.start()
            }
            mediaPlayer?.setOnCompletionListener {
                it.release()
                mediaPlayer = null
                currentUrl = null
                onPlaybackComplete?.invoke()
            }
            currentUrl = url
        } catch (e: Exception) {
            Log.e("SoundPlayer", "Error playing sound", e)
        }
    }

    fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentUrl = null
        onPlaybackComplete?.invoke()
    }
}
