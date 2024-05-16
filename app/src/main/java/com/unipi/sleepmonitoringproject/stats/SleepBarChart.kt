package com.unipi.sleepmonitoringproject.stats

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
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


class SleepBarChart(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    // Declare startTime as a global variable
    private var startTime: Calendar = Calendar.getInstance()
    init {
        init()
    }

    private fun init() {
        // Inflate the entire XML layout
        LayoutInflater.from(context).inflate(R.layout.bar_chart, this, true)

        // Find the BarChart by its ID
        val barChart: BarChart = rootView.findViewById(R.id.bar_chart)
        //barChart.setNoDataText("")
        //barChart.invalidate()


        val mTf: Typeface = Typeface.DEFAULT

        val data: BarData = getDataFromActivity()

        Log.d("SBORRA", "data -> $data")

        data.setValueTypeface(mTf)

        setupChart(barChart, data, Color.argb(9, 32, 84, 1))
    }

    private fun setupChart(barChart: BarChart?, data: BarData, color: Int) {
        data.getDataSetByIndex(0) as BarDataSet

        if (barChart != null) {

            // No description text
            //barChart.description.isEnabled = false

            // Disable grid background
            //barChart.setDrawGridBackground(false)

            // Enable touch gestures
            //barChart.setTouchEnabled(true)

            // Enable scaling and dragging
            //barChart.isDragEnabled = true
            //barChart.setScaleEnabled(true)

            // If disabled, scaling can be done on x- and y-axis separately
            //barChart.setPinchZoom(true)

            barChart.setBackgroundColor(color)
            barChart.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

            // Add data
            barChart.setData(data)

            // Get and handle the legend
            //val legend: Legend = barChart.legend
            //legend.isEnabled = true
            //legend.textColor = Color.WHITE

            //barChart.xAxis.isEnabled = true
            //barChart.axisLeft.isEnabled = true

            val xAxis = barChart.xAxis
            barChart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val mFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val millis = startTime.timeInMillis + value.toLong()
                    return mFormat.format(Date(millis))
                }
            }
            xAxis.setDrawLabels(true)
            //xAxis.setDrawAxisLine(false)
            //xAxis.setDrawGridLines(true)
            //xAxis.position = XAxis.XAxisPosition.BOTTOM

            val yAxis = barChart.axisLeft
            // yAxis.valueFormatter = SleepTypeValueFormatter()
//            yAxis.setDrawLabels(true)
//            yAxis.setDrawAxisLine(true)
//            yAxis.setDrawGridLines(false)
//            yAxis.axisMinimum = -0.5f
//            yAxis.axisMaximum = 3.5f

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
            //yAxis.granularity = 1f
            //yAxis.setCenterAxisLabels(true)

            //barChart.axisRight.isEnabled = false
            barChart.animateX(2500)

            barChart.xAxis.textColor = Color.WHITE
            barChart.axisLeft.textColor = Color.WHITE
            //barChart.setViewPortOffsets(150F, 50F, 100F, 100F)

            //barChart.setDrawBorders(false)
            barChart.invalidate()
        }
    }

//    private fun generateBarEntries(): ArrayList<BarEntry> {
//        val values = ArrayList<BarEntry>()
//        val startTime = Calendar.getInstance()
//        startTime.set(2024, Calendar.MAY, 7, 22, 0) // Start date and time of sleep
//        val endTime = Calendar.getInstance()
//        endTime.set(2024, Calendar.MAY, 8, 6, 0) // End date and time of sleep
//
//        val sleepPhaseDuration = 30 * 60 * 1000 // 30 minutes
//
//        val random = Random()
//
//        val currentTime = startTime.clone() as Calendar
//        while (currentTime.before(endTime)) {
//            //val timestamp = currentTime.timeInMillis - startTime.timeInMillis
//            val timestamp = currentTime.timeInMillis
//
//
//            // Generazione casuale del tipo di sonno
//            val sleepType = random.nextInt(4)
//
//            // Adding data to the list of values
//            //val fTimestamp = timestamp.toFloat() / (1000 * 60)
//            val fTimestamp = timestamp.toFloat()
//
//            Log.d("TIMESTAMP", "Timestamp -> $fTimestamp")
//
//            //TODO FIXME
//            val fSleepType = sleepType.toFloat()
//            val newEntry = BarEntry(fTimestamp, fSleepType)
//            values.add(newEntry)
//
//            // Advancing time by a fixed duration for each sleep phase
//            currentTime.add(Calendar.MILLISECOND,sleepPhaseDuration)
//
//        }
//        return values
//    }

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

        startTime = Calendar.getInstance()
        startTime.set(2024, Calendar.MAY, 7, 22, 0) // Start date and time of sleep
        val endTime = Calendar.getInstance()
        endTime.set(2024, Calendar.MAY, 8, 6, 0) // End date and time of sleep

        val sleepPhaseDuration = 1800000L// 30 minutes

        val values = generateRandomData(startTime.timeInMillis, endTime.timeInMillis, sleepPhaseDuration)

        val barDataSet = BarDataSet(values, "The year 2017")

        barDataSet.color = Color.BLUE // set bar color to blue

        val data = BarData(barDataSet)
        //data.setValueTextSize(10f)
        data.barWidth = 0.9f

        return data
    }
}