package com.unipi.sleepmonitoringproject

import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.unipi.sleepmonitoringproject.db.EventManagerDbHelper

class ClientDataViewModelFactory(private val dbHelper: EventManagerDbHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientDataViewModel(dbHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
