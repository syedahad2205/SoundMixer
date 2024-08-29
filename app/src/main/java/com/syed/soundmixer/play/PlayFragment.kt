package com.syed.soundmixer.play

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.syed.soundmixer.databinding.FragmentPlayBinding
import com.syed.soundmixer.sound.PlaybackService

class PlayFragment : Fragment() {

    private lateinit var binding: FragmentPlayBinding
    private val playbackCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PlaybackService.ACTION_COMPLETE) {
                binding.playButton.isEnabled = true
                binding.stopButton.isEnabled = false
            }
        }
    }

    companion object {
        private const val ARG_FILE_PATH = "file_path"
        private const val ARG_FILE_NAME = "file_name"

        fun newInstance(filePath: String, fileName: String): PlayFragment {
            val fragment = PlayFragment()
            val args = Bundle().apply {
                putString(ARG_FILE_PATH, filePath)
                putString(ARG_FILE_NAME, fileName)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayBinding.inflate(inflater, container, false)
        val filePath = arguments?.getString(ARG_FILE_PATH)
        binding.soundName.text = arguments?.getString(ARG_FILE_NAME) ?: "No Sound"

        binding.playButton.setOnClickListener {
            if (filePath != null) {
                val playIntent = Intent(requireContext(), PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_PLAY
                    putExtra("file_path", filePath)
                }
                requireContext().startService(playIntent)
            }
            binding.playButton.isEnabled = false
            binding.stopButton.isEnabled = true
        }

        binding.stopButton.setOnClickListener {
            val stopIntent = Intent(requireContext(), PlaybackService::class.java).apply {
                action = PlaybackService.ACTION_STOP
            }
            requireContext().startService(stopIntent)
            binding.playButton.isEnabled = true
            binding.stopButton.isEnabled = false
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(playbackCompleteReceiver, IntentFilter(PlaybackService.ACTION_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(playbackCompleteReceiver)
    }
}
