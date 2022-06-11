package com.example.minorproject.data

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.minorproject.data.Criminal
import com.example.minorproject.data.CriminalDatabase
import com.example.minorproject.data.CriminalDetailDao

class CriminalDetailRepository(context: Application) {
    private val criminalDetailDao: CriminalDetailDao = CriminalDatabase.getDatabase(context).criminalDetailDao()

    fun getCriminal(id: Long): LiveData<Criminal>{
        return criminalDetailDao.getCriminal(id)
    }

    suspend fun insertCriminal(criminal: Criminal): Long{
        return criminalDetailDao.insertCriminal(criminal)
    }

    suspend fun updateCriminal(criminal: Criminal){
        criminalDetailDao.updateCriminal(criminal)
    }

    suspend fun deleteCriminal(criminal: Criminal){
        criminalDetailDao.deleteCriminal(criminal)
    }
}