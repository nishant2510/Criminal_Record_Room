package com.example.minorproject.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.minorproject.data.Criminal
import com.example.minorproject.data.CriminalListRepository

class CriminalListViewModel(application: Application): AndroidViewModel(application) {
    private val repo: CriminalListRepository = CriminalListRepository(application)

    val criminals: LiveData<List<Criminal>> = repo.getCriminals()

    suspend fun insertCriminals(employees: List<Criminal>){
        repo.insertCriminals(employees)
    }

    suspend fun getCriminalList(): List<Criminal>{
        return repo.getCriminalList()
    }
}