package com.syed.soundmixer.models

import com.syed.soundmixer.models.Sound

data class SearchResponse(
    val results: List<Sound>,
    val next : String?
)