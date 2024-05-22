package com.unipi.sleepmonitoringproject

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.lifecycle.ViewModel

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
import com.unipi.sleepmonitoring_masss_library.db.insertIntoDB
import com.unipi.sleepmonitoringproject.db.EventManagerContract
import com.unipi.sleepmonitoringproject.db.EventManagerDbHelper
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ClientDataViewModel(private val dbHelper: EventManagerDbHelper):
    ViewModel(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener
{
    var updateGUI: (series: TimeSeries) -> Unit = {}
    // Creazione di un pool di thread
    private val executorService = Executors.newFixedThreadPool(4)
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.i("DATA", "ARGGG")

        dataEvents.forEach { event ->
            Log.i("DATA", "$event from ${event.dataItem.uri.path}")
            if (event.dataItem.uri.path != "/ping-pong")
                return@forEach

            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
            val db = dbHelper.writableDatabase

            // Not a time-series
            if (!dataMap.containsKey("start")) {
                Log.i("DATA", "Not a time-series!")
                return@forEach
            }

            // insert into db
            executorService.execute {
                val dataMapItem = dataMap.getDataMap("combined_series") ?: DataMap()
                val series = TimeSeries.deserializeFromGoogle(dataMapItem)
                insertIntoDB(db, series)
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        executorService.shutdown()
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow()
            }
        } catch (ex: InterruptedException) {
            executorService.shutdownNow()
            Thread.currentThread().interrupt()
        } finally {
            //db.close() // Chiudi il database qui
        }
    }

    override fun onMessageReceived(p0: MessageEvent) {
        TODO("Not yet implemented")
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        TODO("Not yet implemented")
    }
}