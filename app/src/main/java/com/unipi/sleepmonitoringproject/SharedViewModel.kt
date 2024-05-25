package com.unipi.sleepmonitoringproject

import androidx.lifecycle.ViewModel
import com.unipi.sleepmonitoring_masss_library.db.EventManagerDbHelper

class SharedViewModel : ViewModel() {
    var dbHelper: EventManagerDbHelper? = null
}