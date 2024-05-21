package com.unipi.sleepmonitoring_masss_library

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test

class ClassifierTest {
    lateinit var timeSeries: TimeSeries
    lateinit var instrumentationContext: Context

    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context

        timeSeries = TimeSeries()

        timeSeries.add(floatArrayOf(32.0f, 0.0f, 0.0f, 0.0f), 0*60*1_000L)
        timeSeries.add(floatArrayOf(35.0f, 0.0f, 0.0f, 0.0f), 3*60*1_000L)
        timeSeries.add(floatArrayOf(40.0f, 0.0f, 0.0f, 0.0f), 7*60*1_000L)
        timeSeries.add(floatArrayOf(45.0f, 0.0f, 0.0f, 0.0f), 9*60*1_000L)

        timeSeries.add(floatArrayOf(78.0f, 0.0f, 0.0f, 0.0f), 11*60*1_000L)
        timeSeries.add(floatArrayOf(75.0f, 0.0f, 0.0f, 0.0f), 14*60*1_000L)
        timeSeries.add(floatArrayOf(40.0f, 0.0f, 0.0f, 0.0f), 17*60*1_000L)

        timeSeries.add(floatArrayOf(95.0f, 3.0f, 2.0f, 3.0f), 24*60*1_000L)
        timeSeries.add(floatArrayOf(124.0f, 2.0f, 3.0f, 2.0f), 27*60*1_000L)
        timeSeries.add(floatArrayOf(105.0f, 5.0f, 5.5f, 0.3f), 29*60*1_000L)
    }

    @Test
    fun testStatistical() {
        val classifierStatistical = ClassifierStatistical()
        classifierStatistical.updateSSD(timeSeries)

        val res = classifierStatistical.doInference(arrayOf(floatArrayOf(33.0f, 32.0f, 38.0f)))

        Log.i("TESTING", "test statistical $res")
        assert(res in 0..3)
    }

    @Test
    fun TestML() {
        val classifierML = ClassifierML(instrumentationContext)

        val res = classifierML.doInference(arrayOf(
            floatArrayOf(33.0f, 43.0f, 41.0f, 32.0f, 38.0f),
            floatArrayOf(3.0f, 4.0f, 1.0f, 0.2f, 0.005f),
            floatArrayOf(3.0f, -4.0f, -1.0f, -0.2f, -0.005f),
            floatArrayOf(-3.0f, 4.0f, -1.0f, 0.2f, -0.005f),
        ))

        Log.i("TESTING", "test ML $res")
        assert(res in 0..3)
    }
}