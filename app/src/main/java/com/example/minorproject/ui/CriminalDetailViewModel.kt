package com.example.minorproject.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.minorproject.data.Criminal
import com.example.minorproject.data.CriminalDetailRepository
import kotlinx.coroutines.launch

class CriminalDetailViewModel(application: Application): AndroidViewModel(application) {
    private val repo: CriminalDetailRepository = CriminalDetailRepository(application)

    private val _criminalId = MutableLiveData<Long>(0)
    val criminalId: LiveData<Long>
    get() = _criminalId

    val criminal: LiveData<Criminal> = Transformations.switchMap(_criminalId){id->
        repo.getCriminal(id)
    }

    fun setCriminalId(id: Long){
        if(_criminalId.value!=id){
            _criminalId.value=id
        }
    }

    fun saveCriminal(criminal: Criminal){
        viewModelScope.launch {
            if(_criminalId.value==0L){
                _criminalId.value = repo.insertCriminal(criminal)
            }
            else{
                repo.updateCriminal(criminal)
            }
        }
    }

    fun deleteCriminal(){
        viewModelScope.launch {
            criminal.value?.let{repo.deleteCriminal(it)}
        }
    }
}