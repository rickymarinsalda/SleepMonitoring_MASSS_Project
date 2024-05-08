package com.example.pingapp

import android.annotation.SuppressLint
import com.google.android.gms.wearable.DataMap

class TimeSeries {
    class Datum(
        val timestamp: Long,
        val datum: FloatArray
    )

    private var data = mutableListOf<Datum>()

    fun add(value: FloatArray, timestamp: Long = System.currentTimeMillis()) {
        data.add(Datum(timestamp, value))
    }

    @SuppressLint("VisibleForTests")
    fun serializeToGoogle(): DataMap {
        val ser = DataMap()
        val components = if (data.isEmpty()) 0 else data[0].datum.size - 1

        ser.putLongArray("timestamps", data.map {it.timestamp}.toLongArray())
        for(i in 0..components) {
            ser.putFloatArray("data$i", data.map { it.datum[i] }.toFloatArray())
        }

        return ser
    }
}