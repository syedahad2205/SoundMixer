package com.syed.soundmixer.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syed.soundmixer.models.Sound
import com.syed.soundmixer.service.ApiService
import com.syed.soundmixer.service.AuthenticatedRetrofit
import com.syed.soundmixer.service.NormalRetrofit
import com.syed.soundmixer.service.TokenResponse
import com.syed.soundmixer.sound.SoundDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val soundDownloader: SoundDownloader,
    @NormalRetrofit private val apiServiceNA: ApiService,
    @AuthenticatedRetrofit private val apiService: ApiService
) : ViewModel() {
    private val _searchResults = MutableLiveData<List<Sound>>()
    val searchResults: LiveData<List<Sound>> = _searchResults

    private val _searchResultsError = MutableLiveData<String>()
    val searchResultsError: LiveData<String> = _searchResultsError

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private var nextPageUrl: String? = null
    private var isFetchingNextPage: Boolean = false

    private var accessToken: String = "VcpQ485XLIHnqv5lktXcOiS7qRS9Im"
    private var refreshToken: String = "DD2qLxsCBxxOpnVylxWkQ9UTal3wQf"

    fun searchSounds(query: String) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = apiService.searchSounds(query)
                nextPageUrl = results.next
                _searchResults.postValue(results.results)
            } catch (e: Exception) {
                _searchResultsError.postValue(e.message)
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun loadNextPage() {
        if (nextPageUrl == null || isFetchingNextPage) return

        _loading.value = true
        isFetchingNextPage = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = apiService.searchSoundViaUrl(nextPageUrl ?: "")
                nextPageUrl = results.next

                val updatedResults = _searchResults.value.orEmpty() + results.results
                _searchResults.postValue(updatedResults)
            } catch (e: Exception) {
                _searchResultsError.postValue(e.message)
            } finally {
                _loading.postValue(false)
                isFetchingNextPage = false
            }
        }
    }

    private fun refreshAccessToken(onSuccess: (TokenResponse) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiServiceNA.refreshToken(
                    clientId = "LiHUrabuw2r9jFjfifX6",
                    clientSecret = "7Kc2kZlKeF8NHgebt8JswZSzXiZxeYil1ZKO4P2P",
                    grantType = "refresh_token",
                    refreshToken = refreshToken
                )
                onSuccess(response)
            } catch (e: Exception) {
                _searchResultsError.postValue(e.message)
            }
        }
    }

    fun downloadSound(
        soundId: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                downloadSoundToLocal(
                    soundId,
                    onSuccess = { onSuccess.invoke() },
                    onError = { onError.invoke(it) })
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    refreshAccessToken {
                        accessToken = it.access_token
                        refreshToken = it.refresh_token
                        downloadSound(soundId, onSuccess, onError)
                    }
                } else {
                    onError(e)
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    private fun downloadSoundToLocal(
        soundId: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            soundDownloader.downloadSound(
                accessToken = accessToken, soundId = soundId,
                onSuccess = {
                    onSuccess.invoke()
                },
                onError = { error ->
                    onError.invoke(error)
                }
            )
        }
    }
}