package com.unipi.sleepmonitoring_masss_library.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.unipi.sleepmonitoring_masss_library.TimeSeries
import com.unipi.sleepmonitoring_masss_library.TimeSeries.Datum
import java.util.function.Consumer

fun insertIntoDB(db: SQLiteDatabase, series: TimeSeries) {
    db.beginTransaction()

    try {
        series.data.forEach(Consumer { datum: Datum ->
            var hiv = ContentValues()
            hiv.put("timestamp", datum.timestamp)
            hiv.put("bpm", datum.datum[0])

            var ret = db.insert("HeartRate", null, hiv)
            if (ret == -1L) throw RuntimeException("HEART RATE FALLITO INSERIMENTO BRUH³")

            hiv = ContentValues()
            hiv.put("timestamp", datum.timestamp)
            hiv.put("acceleration_x", datum.datum[1])
            hiv.put("acceleration_y", datum.datum[2])
            hiv.put("acceleration_z", datum.datum[3])
            ret = db.insert("Motion", null, hiv)
            if (ret == -1L) throw RuntimeException("HEART RATE FALLITO INSERIMENTO BRUH³")
        })
        db.setTransactionSuccessful()
    } finally {
        db.endTransaction()
    }
}

fun clearDatabase(dbHelper: EventManagerDbHelper) {
    val db = dbHelper.writableDatabase
    try {
        db.beginTransaction()
        val tables = arrayOf(
            EventManagerContract.SleepEvent.TABLE_NAME1,
            EventManagerContract.SleepEvent.TABLE_NAME2,
        )

        for (table in tables) {
            val deletedRows = db.delete(table, null, null)
            Log.i("DatabaseClear", "Deleted $deletedRows rows from table $table")
        }

        db.setTransactionSuccessful()
        db.setTransactionSuccessful()
    } catch (e: Exception) {
        Log.e("DatabaseClear", "Error while trying to clear database", e)
    } finally {
        db.endTransaction()
        db.close()
    }
}