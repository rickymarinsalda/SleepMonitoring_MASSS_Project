package com.example.pingapp

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.pingapp.db.EventManagerContract
import com.example.pingapp.db.EventManagerDbHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.unipi.sleepmonitoring_masss_library.TimeSeries
import java.text.SimpleDateFormat

class ClientDataViewModel(private val dbHelper: EventManagerDbHelper):
    ViewModel(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener
{
    var updateGUI: (series: TimeSeries) -> Unit = {}

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.i("DATA", "ARGGG")


        dataEvents.forEach {
            Log.i("DATA", "$it from ${it.dataItem.uri.path}")
            if(it.dataItem.uri.path != "/ping-pong")
                return

            val dataMap = DataMapItem.fromDataItem(it.dataItem).dataMap
            val db = dbHelper.writableDatabase
            // Not a time-series
            if(! dataMap.containsKey("start")) {
                Log.i("DATA", "Not a time-series!")
                return
            }

            if(dataMap.containsKey("data_accel")) { // momentaneamente data_accel perchè heart non va
                Log.i("TEST", "SONO ENTRATO IN DATA_HEART")
                val dataHeart = dataMap.getDataMap("data_accel") ?: DataMap()
                val series = TimeSeries.deserializeFromGoogle(dataHeart)
                Log.i("TEST", "SERIES:"+ series)
                if(series.size() > 0) {
                    val thread = Thread {
                        val db = dbHelper.writableDatabase
                        for (i in 0 until series.size()) {
                            val datum = series.get(i)
                            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(datum.timestamp)
                            insertDataIntoDatabaseHeartRate(db, timestamp, datum.datum[0].toDouble())
                        }
                        db.close()
                    }
                    thread.start()
                } else {
                    Log.i("DATA", "No data received")
                }
            } else {
                Log.i("DATA", "Missing data_heart in DataMap")
            }

            updateGUI(TimeSeries.deserializeFromGoogle(dataMap.getDataMap("data_accel")!!))

        }


    }

    override fun onMessageReceived(p0: MessageEvent) {
        TODO("Not yet implemented")
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        TODO("Not yet implemented")
    }

    private fun insertDataIntoDatabaseHeartRate(db: SQLiteDatabase, timestamp: String, bpm: Double) {
        Log.i("TEST", "INSERISCO NEL DB")
        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP, timestamp)
            put(EventManagerContract.SleepEvent.COLUMN_NAME_EVENT1, bpm)
        }

        // Insert the new row, returning the primary key value of the new row
        val newRowId = db.insert(EventManagerContract.SleepEvent.TABLE_NAME1, null, values)

    }
}