package com.syed.soundmixer.models

import com.google.gson.annotations.SerializedName

data class Previews(
    @SerializedName("preview-hq-mp3")
    val previewUrl: String
)
