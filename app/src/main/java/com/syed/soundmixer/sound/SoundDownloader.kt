package com.syed.soundmixer.sound

import android.content.Context
import com.syed.soundmixer.room.SavedSound
import com.syed.soundmixer.room.SavedSoundsDao
import com.syed.soundmixer.service.ApiService
import com.syed.soundmixer.service.AuthenticatedRetrofit
import com.syed.soundmixer.service.NormalRetrofit
import com.syed.soundmixer.service.TokenResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class SoundDownloader(
    private val context: Context,
    private val savedSoundsDao: SavedSoundsDao,
    private val apiServiceNA: ApiService
) {

    suspend fun downloadSound(
        accessToken: String,
        soundId: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val response: Response<ResponseBody> = apiServiceNA.downloadSound(soundId, "Bearer $accessToken")
            if (response.code() == 401){
                //refresh token
            }
            if (response.isSuccessful) {
                val responseBody: ResponseBody = response.body() ?: throw Exception("Response body is null")

                val contentDisposition = response.headers()["Content-Disposition"]
                val fileName = contentDisposition?.split("=")?.get(1)?.replace("\"", "") ?: "unknown_file"

                val file = File(context.getExternalFilesDir(null), fileName)

                withContext(Dispatchers.IO) {
                    FileOutputStream(file).use { outputStream ->
                        responseBody.byteStream().use { inputStream ->
                            val buffer = ByteArray(1024)
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                }

                val savedSound = SavedSound(id = soundId, fileName = fileName, filePath = file.absolutePath)
                savedSoundsDao.insert(savedSound)
                onSuccess()
            } else {
                onError(Exception("Download failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            onError(e)
        }
    }
}
