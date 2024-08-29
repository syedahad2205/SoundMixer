package com.syed.soundmixer.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syed.soundmixer.room.SavedSound
import com.syed.soundmixer.room.SavedSoundsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val savedSoundsDao: SavedSoundsDao
) : ViewModel() {
    private val _audioFile1 = MutableLiveData<String?>()
    val audioFile1: LiveData<String?> get() = _audioFile1

    private val _audioFile2 = MutableLiveData<String?>()
    val audioFile2: LiveData<String?> get() = _audioFile2

    private val _selectedFiles = MutableLiveData<MutableList<String>>(mutableListOf())
    val selectedFiles: LiveData<MutableList<String>> = _selectedFiles

    fun toggleSelection(filePath: String) {
        _selectedFiles.value?.let {
            if (it.contains(filePath)) {
                it.remove(filePath)
            } else {
                if (it.size < 2) {
                    it.add(filePath)
                }
            }
            _selectedFiles.value = it
        }
    }

    fun setAudioFiles(filePath1: String?, filePath2: String?) {
        _audioFile1.value = filePath1
        _audioFile2.value = filePath2
    }

    fun saveFileInRoom(savedSound: SavedSound) {
        viewModelScope.launch {
            try {
                savedSoundsDao.insert(savedSound)
            } catch (_: Exception) {
            }
        }
    }
}
