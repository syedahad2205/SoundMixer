package com.syed.soundmixer.sound

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.syed.soundmixer.R

class PlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val ACTION_PLAY = "com.syed.soundmixer.play.ACTION_PLAY"
        const val ACTION_STOP = "com.syed.soundmixer.play.ACTION_STOP"
        const val CHANNEL_ID = "PlaybackChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_COMPLETE = "com.syed.soundmixer.play.ACTION_COMPLETE"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val filePath = intent.getStringExtra("file_path")
                if (filePath != null) {
                    playMedia(filePath)
                }
            }

            ACTION_STOP -> stopMedia()
        }
        return START_NOT_STICKY
    }

    private fun playMedia(filePath: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener {
                    start()
                    showNotification()
                }
                setOnCompletionListener {
                    sendBroadcast(Intent(ACTION_COMPLETE))
                    stopSelf()
                }
                setOnErrorListener { _, what, extra ->
                    handleMediaPlayerError(what, extra)
                    stopSelf()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("MediaPlayerError", "Error code: ${e.message}, Extra code: ${e.stackTrace}")
        }
    }

    private fun handleMediaPlayerError(what: Int, extra: Int) {
        Toast.makeText(
            this,
            "MediaPlayerError : Error code: $what, Extra code: $extra",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun stopMedia() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopForeground(true)
        stopSelf()
    }

    private fun showNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Playback Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val playIntent = Intent(this, PlaybackService::class.java).apply {
            action = ACTION_PLAY
        }
        val playPendingIntent = PendingIntent.getService(
            this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, PlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val playAction = NotificationCompat.Action.Builder(
            R.drawable.ic_play, "Play", playPendingIntent
        ).build()

        val stopAction = NotificationCompat.Action.Builder(
            R.drawable.ic_stop, "Stop", stopPendingIntent
        ).build()

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sound Mixer")
            .setContentText("Playing Audio")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(playAction)
            .addAction(stopAction)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
