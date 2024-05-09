package com.unipi.sleepmonitoring_masss_library

import android.annotation.SuppressLint
import com.google.android.gms.wearable.DataMap

class TimeSeries {
    class Datum(
        val timestamp: Long,
        val datum: FloatArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Datum

            if (timestamp != other.timestamp) return false
            if (!datum.contentEquals(other.datum)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = timestamp.hashCode()
            result = 31 * result + datum.contentHashCode()
            return result
        }
    }

    private var data = mutableListOf<Datum>()

    fun add(value: FloatArray, timestamp: Long = System.currentTimeMillis()) {
        data.add(Datum(timestamp, value))
    }

    fun get(i: Int): Datum {
        return data[i]
    }

    fun size(): Int {
        return data.size
    }

    @SuppressLint("VisibleForTests")
    fun serializeToGoogle(): DataMap {
        val ser = DataMap()
        val components = if (data.isEmpty()) 0 else data[0].datum.size

        ser.putLongArray("timestamps", data.map {it.timestamp}.toLongArray())
        for(i in 0..components - 1) {
            ser.putFloatArray("data$i", data.map { it.datum[i] }.toFloatArray())
        }
        ser.putInt("components", components)

        return ser
    }

    companion object {
        @SuppressLint("VisibleForTests")
        fun deserializeFromGoogle(serialized: DataMap): TimeSeries {
            val series = TimeSeries()
            val timestamps = serialized.getLongArray("timestamps")!!
            val n_components = serialized.getInt("components")
            val data = Array<FloatArray>(n_components) { floatArrayOf() }

            for (i in 0..n_components - 1) {
                data[i] = serialized.getFloatArray("data$i")!!
                assert(data[i].size == timestamps.size)
            }

            for (i in 0..timestamps.size - 1) {
                val datum = FloatArray(n_components)
                for (j in 0..n_components - 1)
                    datum[j] = data[j][i]

                series.add(datum, timestamps[i])
            }

            return series
        }
    }


}