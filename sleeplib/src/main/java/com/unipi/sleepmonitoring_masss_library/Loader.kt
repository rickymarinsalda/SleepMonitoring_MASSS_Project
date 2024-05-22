package com.unipi.sleepmonitoring_masss_library

import android.content.Context
import android.database.Cursor
import com.unipi.sleepmonitoring_masss_library.db.EventManagerContract
import com.unipi.sleepmonitoring_masss_library.db.EventManagerDbHelper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val INTERPOLATION_INTERVAL = 90_000L
const val INTERPOLATION_MIN_TRIGGER = 60_000L

interface Loader {
    fun loadData(timestampBase: Long = System.currentTimeMillis(), endTime: Long = Long.MAX_VALUE /** @TODO ARG */): TimeSeries
}

class FileLoader(
    private val context: Context,
    private val filenameBPM: String,
    private val filenameAccel: String
) : Loader {
    override fun loadData(timestampBase: Long, endTime: Long): TimeSeries {
        val series = TimeSeries()

        // Legge i dati dal file di testo
        val assetManager = context.assets
        val inputStream = assetManager.open(filenameBPM)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val readerAccel = BufferedReader(InputStreamReader(assetManager.open(filenameAccel)))

        var line: String?
         // Timestamp di partenza
        var lastTimestamp = 0L
        var lines = 0L
        var lastBPM = 0.0f

        while (reader.readLine().also { line = it } != null) {
            line?.let {
                lines++
                val dataParts = it.split(",") // Divide la linea in due parti
                if (dataParts.size != 2) {
                    throw Exception("bruh")
                }

                val timestampGrezzo = (dataParts[0].toFloat() * 1000f).toLong();
                val timestamp =  timestampGrezzo + timestampBase;
                val bpm = dataParts[1].toFloat() // Usa solo la parte dopo la virgola

                if (timestampGrezzo < 0.0f)
                    return@let

                if (bpm.isNaN() or bpm.isInfinite())
                    throw Exception("BRUH IN VIRGOLA MOBILE")

                if (timestamp - lastTimestamp < 2000L && lastTimestamp != 0L)
                    return@let

                // align accel to bpm
                var timestampAccel = 0L
                var dataPartsA: List<String> = emptyList();
                while(timestampAccel < timestampGrezzo)
                {
                    val lineAccel = readerAccel.readLine()
                    dataPartsA = lineAccel.split(" ")
                    if (dataPartsA.size != 3+1) {
                        throw Exception("BRUH³*BRUH")
                    }

                    timestampAccel = (dataParts[0].toFloat() * 1000f).toLong();
                }

                val valuesAccel = floatArrayOf(
                    dataPartsA[1].toFloat(),
                    dataPartsA[2].toFloat(),
                    dataPartsA[3].toFloat()
                )

                val toInsert = mutableListOf<Pair<Long, Float>>()
                if (timestamp - lastTimestamp < INTERPOLATION_MIN_TRIGGER || lastTimestamp == 0L)
                    toInsert.add(Pair(timestamp, bpm))
                else { // Interpola
                    val n_ticks = (timestamp - lastTimestamp)/INTERPOLATION_INTERVAL
                    for (i in 1..n_ticks) {
                        toInsert.add(Pair(
                            timestamp + i*INTERPOLATION_INTERVAL,
                            (1.0f - i.toFloat()/n_ticks)*lastBPM + (i.toFloat()/n_ticks)*bpm
                        ))
                    }
                }

                // Write to series
                series.add(
                    floatArrayOf(bpm) + valuesAccel,
                    timestamp
                )

                lastTimestamp = timestamp
                lastBPM = bpm
            }
        }

        return series
    }
}

class DbLoader(private val dbHelper: EventManagerDbHelper) : Loader {

    /**
     * ACHTUNG! This function may be very sloooow, call it a coroutine and then await()
     */
    override fun loadData(timestampBase: Long, endTime: Long): TimeSeries {
        val series = TimeSeries()

        val cursor = queryDatabase(dbHelper, timestampBase, endTime)
        cursor.use {
            while (it.moveToNext()) {
                val timestamp = it.getLong(it.getColumnIndexOrThrow(EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP)) // QUI LA DATA È STRINGA
                val values = floatArrayOf(
                    it.getFloat(it.getColumnIndexOrThrow(EventManagerContract.SleepEvent.COLUMN_NAME_EVENT1)), //bpm
                    it.getFloat(it.getColumnIndexOrThrow(EventManagerContract.SleepEvent.COLUMN_NAME_EVENT2_x)), //accel_x
                    it.getFloat(it.getColumnIndexOrThrow(EventManagerContract.SleepEvent.COLUMN_NAME_EVENT2_y)), //accel_y
                    it.getFloat(it.getColumnIndexOrThrow(EventManagerContract.SleepEvent.COLUMN_NAME_EVENT2_z)) //accel_z
                )

                // si converte la stringa del timestamp in millisecondi
                //val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                // val date = dateFormat.parse(timestampString)
                //val timestamp = date?.time ?: 0L // QUI LA DATA È IN MILLISEC

                series.add(values,timestamp)
            }
        }
        return series
    }

    private fun queryDatabase(dbHelper: EventManagerDbHelper, start: Long, end: Long): Cursor {
        val db = dbHelper.readableDatabase
        //val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

        //val startString = dateFormat.format(Date(start))
        //val endString = dateFormat.format(Date(end))
        /*val projection = arrayOf(BaseColumns._ID, EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP, EventManagerContract.SleepEvent.COLUMN_NAME_EVENT1)
        val selection = null
        val selectionArgs = null
        //val selection = "${EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP} >= ? AND ${EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP} <= ?" // Filtra in modo che sia una data compresa tra ieri e oggi
        //val selectionArgs = arrayOf(startString, endString)
        val sortOrder = "${EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP} ASC"
        return db.query(
            EventManagerContract.SleepEvent.TABLE_NAME1,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )*/

        return db.rawQuery(
            """
                SELECT HR.timestamp, bpm, acceleration_x, acceleration_y, acceleration_z
                FROM HeartRate HR
                INNER JOIN Motion M on HR.timestamp = M.timestamp
                WHERE HR.timestamp BETWEEN ? AND ?
                ORDER BY HR.timestamp ASC
            """.trimIndent(),
            arrayOf(start.toString(), end.toString())
        )
    }

}
