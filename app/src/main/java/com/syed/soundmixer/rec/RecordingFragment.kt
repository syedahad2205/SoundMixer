package com.syed.soundmixer.rec

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.syed.soundmixer.databinding.FragmentRecordingBinding
import com.syed.soundmixer.home.SharedViewModel
import com.syed.soundmixer.room.SavedSound
import com.syed.soundmixer.room.SavedSoundsDao
import com.syed.soundmixer.sound.VisualizerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class RecordingFragment : Fragment() {
    @Inject
    lateinit var savedSoundsDao: SavedSoundsDao
    private lateinit var binding: FragmentRecordingBinding
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioRecord: AudioRecord? = null
    private lateinit var visualizerView: VisualizerView
    private val handler = Handler(Looper.getMainLooper())
    private val updateVisualizerRunnable = object : Runnable {
        override fun run() {
            updateVisualizer()
            handler.postDelayed(this, 200)
        }
    }
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            updateTimer()
            handler.postDelayed(this, 1000)
        }
    }
    private var startTime: Long = 0
    private var filePath: String? = null

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordingBinding.inflate(inflater, container, false)
        visualizerView = binding.visualizerView
        binding.playButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                checkAndRequestPermissions()
            }
        }
        binding.stopButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            }
        }
        binding.timerTextView.text = "00:00"
        return binding.root
    }

    private fun checkAndRequestPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                200
            )
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            filePath = getOutputFilePath()
            setOutputFile(filePath)

            try {
                prepare()
                start()
                isRecording = true
                startTime = System.currentTimeMillis()

                val minBufferSize = AudioRecord.getMinBufferSize(
                    44100,
                    android.media.AudioFormat.CHANNEL_IN_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT
                )

                if (ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    44100,
                    android.media.AudioFormat.CHANNEL_IN_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize
                ).apply {
                    startRecording()
                }

                handler.post(updateVisualizerRunnable)
                handler.post(updateTimerRunnable)

            } catch (e: Exception) {
                Log.e("RecordingFragment", "Error starting recording", e)
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null

        handler.removeCallbacks(updateVisualizerRunnable)
        handler.removeCallbacks(updateTimerRunnable)

        isRecording = false
        binding.timerTextView.text = "00:00"

        visualizerView.reset()

        lifecycleScope.launch {
            val fileName = File(filePath ?: "").name
            val savedSound =
                SavedSound(id = "id-$fileName", fileName = fileName, filePath = filePath ?: "")
            sharedViewModel.saveFileInRoom(savedSound)
        }
    }

    private fun updateVisualizer() {
        val amplitude = mediaRecorder?.maxAmplitude ?: 0
        visualizerView.updateAmplitude(amplitude)
    }

    @SuppressLint("DefaultLocale")
    private fun updateTimer() {
        val elapsedMillis = System.currentTimeMillis() - startTime
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60
        binding.timerTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun getOutputFilePath(): String {
        val directory = requireContext().getExternalFilesDir(null)
        val prefixedFileName = "per-${System.currentTimeMillis()}.wav"
        return File(directory, prefixedFileName).absolutePath
    }
}
