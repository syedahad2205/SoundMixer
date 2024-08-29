package com.syed.soundmixer.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SavedSoundsDao {
    @Insert
    suspend fun insert(savedSound: SavedSound)

    @Query("SELECT * FROM downloaded_sounds WHERE id = :id")
    suspend fun getById(id: String): SavedSound?

    @Query("DELETE FROM downloaded_sounds WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM downloaded_sounds")
    suspend fun getAllSavedSounds(): List<SavedSound>
}
