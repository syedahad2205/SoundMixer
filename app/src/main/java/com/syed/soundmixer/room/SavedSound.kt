package com.syed.soundmixer.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_sounds")
data class SavedSound(
    @PrimaryKey val id: String,
    val fileName: String,
    val filePath: String
)
