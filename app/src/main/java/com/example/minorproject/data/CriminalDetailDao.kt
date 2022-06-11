package com.example.minorproject.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.minorproject.data.Criminal

@Dao
interface CriminalDetailDao {
    @Query("SELECT * FROM criminal WHERE id = :id")
    fun getCriminal(id: Long): LiveData<Criminal>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCriminal(criminal: Criminal): Long

    @Update
    suspend fun updateCriminal(criminal: Criminal)

    @Delete
    suspend fun deleteCriminal(criminal: Criminal)
}