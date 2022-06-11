package com.example.minorproject.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.minorproject.data.Criminal

@Dao
interface CriminalListDao {
    @Query("SELECT * FROM criminal ORDER BY name")
    fun getCriminals(): LiveData<List<Criminal>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCriminals(criminals: List<Criminal>)

    @Query("SELECT * FROM criminal ORDER BY name")
    suspend fun getCriminalList(): List<Criminal>
}