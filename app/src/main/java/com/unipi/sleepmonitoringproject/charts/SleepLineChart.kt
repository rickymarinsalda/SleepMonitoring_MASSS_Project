package com.unipi.sleepmonitoringproject.charts

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.unipi.sleepmonitoring_masss_library.ClassifierML
import com.unipi.sleepmonitoring_masss_library.TimeSeries
import com.unipi.sleepmonitoring_masss_library.classifySeries
import com.unipi.sleepmonitoringproject.R
import java.text.SimpleDateFormat
import java.util.Calendar.*

class SleepLineChart(val rootView: View, val lastNightData: TimeSeries) {

    val id: Int = R.id.line_chart
    private val lineChart: LineChart = rootView.findViewById(R.id.line_chart)
    private lateinit var startTime : Calendar
    private lateinit var endTime : Calendar
    private var startTimeAsleep: Double = 0.0
    private var deepSleepTotal: Double = 0.0
    private var lightSleepTotal: Double = 0.0
    private var remSleepTotal: Double = 0.0
    private var awakeTotal: Double = 0.0

    init {

        val mTf: Typeface = Typeface.DEFAULT
        val data: LineData = getData()
        data.setValueTypeface(mTf)

        setupChart(lineChart, data, Color.argb(9, 32, 84, 1))
    }

    private fun setupChart(lineChart: LineChart?, data: LineData, color: Int) {
        data.getDataSetByIndex(0) as LineDataSet

        if (lineChart != null) {

            // No description text
            lineChart.description.isEnabled = false

            // Disable grid background
            lineChart.setDrawGridBackground(false)

            // Enable touch gestures
            lineChart.setTouchEnabled(true)

            // Enable scaling and dragging
            lineChart.isDragEnabled = true
            lineChart.setScaleEnabled(true)

            // If disabled, scaling can be done on x- and y-axis separately
            lineChart.setPinchZoom(true)

            lineChart.setBackgroundColor(color)

            // Add data
            lineChart.data = data

            // Disable legend
            lineChart.legend.isEnabled = false

            // Customize axis view
            lineChart.xAxis.isEnabled = true
            lineChart.axisLeft.isEnabled = true

            // Customize x-axis
            val xAxis = lineChart.xAxis
            xAxis.valueFormatter = SleepTimestampFormatter()
            xAxis.setDrawLabels(true)
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE
            lineChart.animateX(2500)

            // Customize y-axis
            val yAxis = lineChart.axisLeft
            yAxis.setDrawLabels(true)
            yAxis.setDrawAxisLine(true)
            yAxis.setDrawGridLines(false)
            yAxis.axisMinimum = -0.5f
            yAxis.axisMaximum = 3.5f
            yAxis.granularity = 1f
            yAxis.setCenterAxisLabels(true)
            lineChart.axisRight.isEnabled = false
            lineChart.axisLeft.textColor = Color.WHITE

            // Customize line chart
            val labels = listOf("Deep","REM", "Light", "Wake")
            yAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < labels.size) {
                        labels[index]
                    } else {
                        ""
                    }
                }
            }

            lineChart.setViewPortOffsets(150F, 50F, 100F, 100F)
            lineChart.setDrawBorders(false)
        }
    }


    private fun getData(): LineData {

        // Generate data and calculate sleep totals
        val values = generateFullNightData()
        calculateSleepTotals(values)

        // Customize line chart
        val set1 = LineDataSet(values, "Last night of sleep")
        set1.lineWidth = 1.75f
        set1.setDrawCircles(false)
        set1.circleRadius = 0f
        set1.circleHoleRadius = 0f
        set1.color = Color.WHITE
        set1.highLightColor = Color.WHITE
        set1.setDrawValues(false)
        set1.setDrawFilled(true)
        set1.fillColor = Color.rgb(83, 99, 135)
        set1.mode = LineDataSet.Mode.CUBIC_BEZIER

        return LineData(set1)
    }

    private fun calculateSleepTotals(sleepData: ArrayList<Entry>) {
        val sleepTotals = mutableMapOf<Int, Double>().withDefault { 0.0 }

        for (entry in sleepData) {
            val sleepType = entry.y.toInt()
            val sleepDuration = sleepTotals.getValue(sleepType)
            sleepTotals[sleepType] = sleepDuration + 0.5 // Add 0.5 minutes for each entry
        }

        deepSleepTotal = sleepTotals[0] ?: 0.0
        remSleepTotal = sleepTotals[1] ?: 0.0
        lightSleepTotal = sleepTotals[2] ?: 0.0
        awakeTotal = sleepTotals[3] ?: 0.0
        startTimeAsleep = findFirstAsleepTimestamp(sleepData)
    }

    private fun findFirstAsleepTimestamp(sleepData: ArrayList<Entry>): Double {

        for (entry in sleepData) {
            val sleepType = entry.y.toInt()
            if (sleepType != 3) { // Not awake
                return entry.x.toDouble()
            }
        }
        return sleepData.firstOrNull()?.x?.toDouble() ?: 0.0
    }

    private fun generateFullNightData(): ArrayList<Entry> {
        val values = ArrayList<Entry>()

        // Create a new classifier instance
        val classifier = ClassifierML(rootView.context)

        // Get the start and end timestamps for the last night
        val res = classifySeries(classifier, lastNightData)
        startTime = getInstance()
        endTime = getInstance()
        endTime.timeInMillis = lastNightData.data[0].timestamp
        startTime.timeInMillis = lastNightData.data[0].timestamp
        val startTimestamp = startTime.clone() as Calendar

        // Invert the y values here
        for (i in res.indices) {
            values.add(Entry(startTimestamp.timeInMillis.toFloat(), 3 - res[i].toFloat())) // Invert y values

            //if the next element exists:
            if(i+1 < res.size)
                startTimestamp.add(MINUTE, 10) // Add 10 minutes to the start timestamp
        }
        endTime = startTimestamp
        return values
    }

    fun getStartTime() : Calendar {
        return startTime
    }

    fun getEndTime() : Calendar {
        return endTime
    }

    fun getStartTimeAsleep(): Double {
        return startTimeAsleep
    }
}

class SleepTimestampFormatter : ValueFormatter() {
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        return dateFormat.format(Date(value.toLong()))
    }
}

fun getStartOfYesterday(timestamp: Long): Long {
    val calendar = getInstance()
    calendar.timeInMillis = timestamp
    calendar.add(DAY_OF_YEAR, -1) // Sottraggo un giorno
    calendar.set(HOUR_OF_DAY, 0)
    calendar.set(MINUTE, 0)
    calendar.set(SECOND, 0)
    calendar.set(MILLISECOND, 0)
    return calendar.timeInMillis
}

fun getEndOfDay(timestamp: Long): Long {
    val calendar = getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(HOUR_OF_DAY, 23)
    calendar.set(MINUTE, 59)
    calendar.set(SECOND, 59)
    calendar.set(MILLISECOND, 999)
    return calendar.timeInMillis
}