package com.example.minorproject.data

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.minorproject.data.Criminal
import com.example.minorproject.data.CriminalDatabase
import com.example.minorproject.data.CriminalListDao

class CriminalListRepository(context:Application){
    private val criminalListDao: CriminalListDao = CriminalDatabase.getDatabase(context).criminalListDao()

    fun getCriminals(): LiveData<List<Criminal>> = criminalListDao.getCriminals()

    suspend fun insertCriminals(employees: List<Criminal>){
        criminalListDao.insertCriminals(employees)
    }

    suspend fun getCriminalList(): List<Criminal>{
        return criminalListDao.getCriminalList()
    }
}