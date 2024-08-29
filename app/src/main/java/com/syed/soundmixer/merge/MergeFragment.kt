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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
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
                mergeAudioFiles(
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

    private fun mergeAudioFiles(filePath1: String, filePath2: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.mergeButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val outputFilePath = withContext(Dispatchers.IO) {
                    val outputFilePath = getOutputFilePath()
                    val file1 = File(filePath1)
                    val file2 = File(filePath2)

                    if (!file1.exists() || !file2.exists()) {
                        throw IOException("One or both files do not exist.")
                    }

                    FileInputStream(file1).use { inputStream1 ->
                        FileInputStream(file2).use { inputStream2 ->
                            FileOutputStream(outputFilePath).use { outputStream ->

                                // Write the header from the first file to the output file
                                val header1 = ByteArray(44)
                                inputStream1.read(header1)
                                outputStream.write(header1)

                                // Copy the data from the first file to the output file
                                val buffer = ByteArray(4096)
                                var bytesRead: Int
                                while (inputStream1.read(buffer).also { bytesRead = it } != -1) {
                                    outputStream.write(buffer, 0, bytesRead)
                                }

                                // Skip the header of the second file (44 bytes)
                                inputStream2.skip(44)

                                // Append the data from the second file to the output file
                                while (inputStream2.read(buffer).also { bytesRead = it } != -1) {
                                    outputStream.write(buffer, 0, bytesRead)
                                }

                                updateWavHeader(outputFilePath)
                            }
                        }
                    }

                    outputFilePath
                }

                saveMergedFileToDatabase(outputFilePath)

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

    private fun updateWavHeader(outputFilePath: String) {
        RandomAccessFile(outputFilePath, "rw").use { raf ->
            val totalAudioLen = raf.length() - 44
            val totalDataLen = totalAudioLen + 36

            raf.seek(4)
            raf.writeInt(totalDataLen.toInt())
            raf.seek(40)
            raf.writeInt(totalAudioLen.toInt())
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
