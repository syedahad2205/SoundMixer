package com.syed.soundmixer.saved_sounds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.syed.soundmixer.databinding.FragmentSavedSoundsBinding
import com.syed.soundmixer.home.HomeActivity
import com.syed.soundmixer.home.SharedViewModel
import com.syed.soundmixer.merge.MergeFragment
import com.syed.soundmixer.play.PlayFragment
import com.syed.soundmixer.room.AppDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedSoundsFragment : Fragment() {

    private lateinit var binding: FragmentSavedSoundsBinding
    private lateinit var adapter: SavedSoundsAdapter
    private val selectedFiles = mutableSetOf<String>()

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedSoundsBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        binding.mergeButton.setOnClickListener {
            if (selectedFiles.size == 2) {
                val mergeFragment = MergeFragment.newInstance()
                sharedViewModel.setAudioFiles(selectedFiles.toList()[0], selectedFiles.toList()[1])
                val viewPager = (requireActivity() as HomeActivity).binding.viewPager
                viewPager.currentItem = 3
                (requireActivity() as HomeActivity).updateMergeFragment(mergeFragment)
            }
        }

        return binding.root
    }

    override fun onPause() {
        selectedFiles.clear()
        binding.mergeButton.visibility = INVISIBLE
        adapter.updateSelectedFiles(selectedFiles)
        super.onPause()
    }

    override fun onResume() {
        loadSavedSounds()
        if (::adapter.isInitialized) {
            adapter.updateSelectedFiles(selectedFiles)
        }
        super.onResume()
    }

    private fun loadSavedSounds() {
        val database = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val savedSounds = database.savedSoundsDao().getAllSavedSounds()
            adapter = SavedSoundsAdapter(requireContext(), savedSounds, onClick = { sound ->
                val playFragment = PlayFragment.newInstance(sound.filePath, sound.fileName)
                val viewPager = (requireActivity() as HomeActivity).binding.viewPager
                viewPager.currentItem = 4
                (requireActivity() as HomeActivity).updatePlayFragment(playFragment)
            }, onLongClick = { sound ->
                handleFileSelection(sound.filePath)
            })
            binding.recyclerView.adapter = adapter
        }
    }

    private fun handleFileSelection(filePath: String) {
        if (selectedFiles.contains(filePath)) {
            selectedFiles.remove(filePath)
        } else {
            if (selectedFiles.size < 2) {
                selectedFiles.add(filePath)
            }
        }
        binding.mergeButton.visibility = if (selectedFiles.size == 2) View.VISIBLE else INVISIBLE
        adapter.updateSelectedFiles(selectedFiles)
    }
}

