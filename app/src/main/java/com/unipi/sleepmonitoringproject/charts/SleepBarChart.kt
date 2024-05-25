package com.unipi.sleepmonitoringproject.charts

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.unipi.sleepmonitoringproject.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random


class SleepBarChart(view: View){
    // Declare startTime as a global variable
    private var startTime: Calendar = Calendar.getInstance()
    private var endTime : Calendar = Calendar.getInstance()
    private var startTimeAsleep: Long = -1
    private var deepSleepTotal: Double = 0.0
    private var lightSleepTotal: Double = 0.0
    private var remSleepTotal: Double = 0.0
    private var awakeTotal: Double = 0.0
    private var sleepPhaseDuration = 1800000L// 30 minutes

    val id: Int = R.id.bar_chart

    private var barChart: BarChart = view.findViewById(R.id.bar_chart)

    init {
        init()
    }

    private fun init() {
        // Inflate the entire XML layout

        // Find the BarChart by its ID
        val mTf: Typeface = Typeface.DEFAULT

        val data: BarData = getDataFromActivity()

        data.setValueTypeface(mTf)

        setupChart(barChart, data, Color.argb(9, 32, 84, 1))
    }

    private fun setupChart(barChart: BarChart?, data: BarData, color: Int) {
        data.getDataSetByIndex(0) as BarDataSet

        if (barChart != null) {

            // No description text
            barChart.description.isEnabled = false

            // Disable grid background
            barChart.setDrawGridBackground(false)

            // Enable touch gestures
            barChart.setTouchEnabled(true)

            // Enable scaling and dragging
            barChart.isDragEnabled = true
            barChart.setScaleEnabled(true)

            // If disabled, scaling can be done on x- and y-axis separately
            barChart.setPinchZoom(true)

            barChart.setBackgroundColor(color)
            barChart.setLayerType(View.LAYER_TYPE_SOFTWARE, null)


            // Get and handle the legend
            val legend: Legend = barChart.legend
            legend.isEnabled = false
            //legend.textColor = Color.WHITE

            barChart.xAxis.isEnabled = true
            barChart.axisLeft.isEnabled = true

            val xAxis = barChart.xAxis
            barChart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val mFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val millis = startTime.timeInMillis + (value.toLong() * sleepPhaseDuration)
                    return mFormat.format(Date(millis))
                }
            }
            xAxis.setDrawLabels(true)
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM

            // Add data
            barChart.setData(data)

            val yAxis = barChart.axisLeft
            // yAxis.valueFormatter = SleepTypeValueFormatter()
            yAxis.setDrawLabels(true)
            yAxis.setDrawAxisLine(true)
            yAxis.setDrawGridLines(false)
            //yAxis.axisMinimum = -0.5f
            //yAxis.axisMinimum = 0f
            //yAxis.axisMaximum = 3.5f

            val labels = listOf("Leggero", "REM", "Profondo", "Veglia")
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
            yAxis.granularity = 1f
            yAxis.setCenterAxisLabels(true)

            barChart.axisRight.isEnabled = false
            barChart.animateX(2500)

            barChart.xAxis.textColor = Color.WHITE
            barChart.axisLeft.textColor = Color.WHITE
            barChart.setViewPortOffsets(150F, 50F, 100F, 100F)

            barChart.setDrawBorders(false)
            barChart.invalidate()
        }
    }

    private fun generateRandomData(startTime: Long, endTime: Long, phase: Long): ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()
        val random = Random()

        var timestamp = startTime
        var index = 0f
        while (timestamp < endTime) {
            val value = random.nextInt(4).toFloat() // random value from 0 to 3
            entries.add(BarEntry(index, value))
            timestamp += phase
            index++
        }

        return entries
    }

    private fun getDataFromActivity(): BarData {

        startTime.set(2024, Calendar.MAY, 7, 22, 0) // Start date and time of sleep
        endTime.set(2024, Calendar.MAY, 8, 6, 0) // End date and time of sleep

        val values = generateRandomData(startTime.timeInMillis, endTime.timeInMillis, sleepPhaseDuration)

        val barDataSet = BarDataSet(values, "The year 2017")

        val data = BarData(barDataSet)
        data.setValueTextSize(0f)
        data.barWidth = 0.9f

        return data
    }

    fun getStartTime() : Calendar {
        return startTime
    }

    fun getEndTime() : Calendar {
        return endTime
    }

    fun getStartTimeAsleep(): Long {
        return startTimeAsleep
    }

    fun getDeepSleepTotal(): Double {
        return deepSleepTotal
    }

    fun getLightSleepTotal(): Double {
        return lightSleepTotal
    }

    fun getRemSleepTotal(): Double {
        return remSleepTotal
    }

    fun getAwakeTotal(): Double {
        return awakeTotal
    }
}