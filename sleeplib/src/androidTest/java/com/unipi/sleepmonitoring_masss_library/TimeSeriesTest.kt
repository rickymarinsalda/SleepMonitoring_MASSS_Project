package com.unipi.sleepmonitoring_masss_library

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class TimeSeriesTest {
    @Test fun testSerialization() {
        val original = TimeSeries()
        for (i in 0..100)
            original.add(floatArrayOf(i*1.0f, i*2.0f, i*-3.0f), i * 1000L)

        val ser = original.serializeToGoogle()
        val deserialized = TimeSeries.deserializeFromGoogle(ser)

        assertTrue("Equal size", original.size() == deserialized.size())

        for (i in 0..100) {
            assertTrue("Element $i equal", original.get(i) == deserialized.get(i))
        }
    }
}