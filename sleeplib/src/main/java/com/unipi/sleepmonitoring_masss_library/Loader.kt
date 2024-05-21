package com.unipi.sleepmonitoring_masss_library

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

const val INTERPOLATION_INTERVAL = 90_000L
const val INTERPOLATION_MIN_TRIGGER = 60_000L

interface Loader {
    fun loadData(timestamp_base: Long = System.currentTimeMillis() /** @TODO ARG */): TimeSeries
}

class FileLoader(
    private val context: Context,
    private val filenameBPM: String,
    private val filenameAccel: String
) : Loader {
    override fun loadData(timestamp_base: Long): TimeSeries {
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
                val timestamp =  timestampGrezzo + timestamp_base;
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
