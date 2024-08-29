package com.syed.soundmixer.merge

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.syed.soundmixer.databinding.FragmentMergeBinding
import com.syed.soundmixer.home.SharedViewModel
import com.syed.soundmixer.room.SavedSound
import com.syed.soundmixer.room.SavedSoundsDao
import com.syed.soundmixer.sound.AudioMediaOperation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

class MergeFragment : Fragment() {

    @Inject
    lateinit var savedSoundsDao: SavedSoundsDao

    private lateinit var binding: FragmentMergeBinding

    private val sharedViewModel: SharedViewModel by activityViewModels()

    companion object {
        fun newInstance(): MergeFragment {
            return MergeFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMergeBinding.inflate(inflater, container, false)

        binding.mergeButton.setOnClickListener {
            if (!sharedViewModel.audioFile1.value.isNullOrEmpty() && !sharedViewModel.audioFile2.value.isNullOrEmpty()) {
                mergeAudioFilesUsingOperations(
                    sharedViewModel.audioFile1.value ?: "",
                    sharedViewModel.audioFile2.value ?: ""
                )
            } else {
                Log.d(
                    "***",
                    "No file found ${sharedViewModel.audioFile1.value} and ${sharedViewModel.audioFile2.value}"
                )
            }
        }

        sharedViewModel.audioFile1.observe(requireActivity()) {
            binding.statusTextView.text =
                if (!sharedViewModel.audioFile1.value.isNullOrEmpty() && !sharedViewModel.audioFile2.value.isNullOrEmpty()) {
                    "Merge files at $it and ${sharedViewModel.audioFile2.value}"
                } else "No sound selected. Please select sounds from Files and merge here"
        }
        return binding.root
    }

    override fun onResume() {
        binding.statusTextView.text =
            if (!sharedViewModel.audioFile1.value.isNullOrEmpty() && !sharedViewModel.audioFile2.value.isNullOrEmpty()) {
                "Merge files at ${sharedViewModel.audioFile1.value} and ${sharedViewModel.audioFile2.value}"
            } else "No sound selected. Please select sounds from Files and merge here"
        super.onResume()
    }

    private fun mergeAudioFilesUsingOperations(filePath1: String, filePath2: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.mergeButton.isEnabled = false

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val outputFilePath = getOutputFilePath()

                    AudioMediaOperation.mergeAudios(
                        selection = arrayOf(filePath1, filePath2),
                        outPath = outputFilePath,
                        onSuccess = {
                            saveMergedFileToDatabase(outputFilePath)
                        }, onFailure = {
                            Log.d("***", "failed")
                        }
                    )
                }

            } catch (e: IOException) {
                Log.e("MergeFragment", "IOException during file merge: ${e.message}")
            } catch (e: Exception) {
                Log.e("MergeFragment", "Unexpected error: ${e.message}")
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.mergeButton.isEnabled = true
                }
            }
        }
    }

    private fun saveMergedFileToDatabase(outputFilePath: String) {
        val fileName = File(outputFilePath).name
        sharedViewModel.saveFileInRoom(
            SavedSound(
                id = "id-m-$fileName",
                fileName = fileName,
                filePath = outputFilePath
            )
        )
    }

    private fun getOutputFilePath(): String {
        val directory = requireContext().getExternalFilesDir(null)
        val fileName = "merged-${System.currentTimeMillis()}.wav"
        return File(directory, fileName).absolutePath
    }
}
