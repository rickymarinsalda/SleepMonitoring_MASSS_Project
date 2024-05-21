package com.unipi.sleepmonitoring_masss_library

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test

class LoaderTest {
    lateinit var instrumentationContext: Context

    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    }
    @Test
    fun testFileLoader () {
        val fileLoader = FileLoader(
            instrumentationContext,
            "8692923_heartrate.txt",
            "8692923_acceleration.txt")

        val series = fileLoader.loadData(0L)

        assert(series.size() > 10)
        assert(series.get(0).datum.size == 4)

        assert(series.get(0).datum[0] == 68.0f)
        assert(series.get(0).datum[1] == -0.6761627f)
        assert(series.get(0).datum[2] == 0.4959564f)
        assert(series.get(0).datum[3] == -0.7410583f)
        assert(series.get(0).timestamp == (4.97290992737 * 1000L).toLong())
    }
}