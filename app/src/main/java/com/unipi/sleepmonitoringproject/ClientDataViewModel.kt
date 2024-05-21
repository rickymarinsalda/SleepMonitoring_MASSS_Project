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
            if (event.dataItem.uri.path != "/ping-pong") return@forEach

            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
            val db = dbHelper.writableDatabase

            // Not a time-series
            if (!dataMap.containsKey("start")) {
                Log.i("DATA", "Not a time-series!")
                return@forEach
            }

            // Funzione per processare i dati
            fun processData(dataKey: String, insertFunction: (SQLiteDatabase, String, FloatArray) -> Unit) {
                if (dataMap.containsKey(dataKey)) {
                    Log.i("TEST", "SONO ENTRATO IN $dataKey")
                    val dataMapItem = dataMap.getDataMap(dataKey) ?: DataMap()
                    val series = TimeSeries.deserializeFromGoogle(dataMapItem)

                    if (series.size() > 0) {
                        executorService.execute {
                            for (i in 0 until series.size()) {
                                val datum = series.get(i)
                                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(datum.timestamp)
                                Log.i("TestTimestamp", "Timestamp: $timestamp")
                                insertFunction(db, timestamp, datum.datum)//datum.datum[0].toDouble()
                            }
                        }
                    } else {
                        Log.i("DATA", "No data received")
                    }
                } else {
                    Log.i("DATA", "Missing $dataKey in DataMap")
                }
            }

            processData("data_heart", ::insertDataIntoDatabaseHeartRate)
            processData("data_accel", ::insertDataIntoDatabaseAccel)

            // Aggiornamento dell'interfaccia utente
            updateGUI(TimeSeries.deserializeFromGoogle(dataMap.getDataMap("data_accel")!!))

            // Chiusura del database dopo che tutti i thread hanno terminato
            //executorService.shutdown()
            //executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
            //db.close()
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

    private fun insertDataIntoDatabaseHeartRate(db: SQLiteDatabase, timestamp: String, bpm: FloatArray) {
        Log.i("TEST", "INSERISCO NEL DB HEART")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = sdf.parse(timestamp)
        val timestamp_int = date.time
        Log.i("TEST", "timestamp che inserirò:" + timestamp_int)
        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP, timestamp_int)
            put(EventManagerContract.SleepEvent.COLUMN_NAME_EVENT1, bpm[0].toDouble())
        }

        // Insert the new row, returning the primary key value of the new row
        val newRowId = db.insert(EventManagerContract.SleepEvent.TABLE_NAME1, null, values)

    }
    private fun insertDataIntoDatabaseAccel(db: SQLiteDatabase, timestamp: String, acc: FloatArray) {
        Log.i("TEST", "INSERISCO NEL DB accel")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = sdf.parse(timestamp)
        val timestamp_int = date.time
        Log.i("TEST", "timestamp che inserirò:" + timestamp_int + "VALORE ACC:" + acc[0].toDouble())
        Log.i("TEST", "timestamp che inserirò:" + timestamp_int + "VALORE ACC:" + acc[1].toDouble())
        Log.i("TEST", "timestamp che inserirò:" + timestamp_int + "VALORE ACC:" + acc[2].toDouble())
        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP, timestamp_int)
            put(EventManagerContract.SleepEvent.COLUMN_NAME_EVENT2_x, acc[0].toDouble())
            put(EventManagerContract.SleepEvent.COLUMN_NAME_EVENT2_y, acc[1].toDouble())
            put(EventManagerContract.SleepEvent.COLUMN_NAME_EVENT2_z, acc[2].toDouble())
        }

        // Insert the new row, returning the primary key value of the new row
        val newRowId = db.insert(EventManagerContract.SleepEvent.TABLE_NAME2, null, values)

    }
}