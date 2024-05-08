package com.unipi.sleepmonitoringproject.stats

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.unipi.sleepmonitoringproject.R
import java.text.SimpleDateFormat

class SleepLineChart(rootView: View) {

    private val lineChart: LineChart = rootView.findViewById(R.id.line_chart)

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

            // Get and handle the legend
            val legend: Legend = lineChart.legend
            legend.isEnabled = true
            legend.textColor = Color.WHITE

            lineChart.xAxis.isEnabled = true
            lineChart.axisLeft.isEnabled = true

            val xAxis = lineChart.xAxis
            xAxis.valueFormatter = SleepTimestampFormatter()
            xAxis.setDrawLabels(true)
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM

            val yAxis = lineChart.axisLeft
            //yAxis.valueFormatter = SleepTypeValueFormatter()
            yAxis.setDrawLabels(true)
            yAxis.setDrawAxisLine(true)
            yAxis.setDrawGridLines(false)
            yAxis.axisMinimum = -0.5f
            yAxis.axisMaximum = 3.5f

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

            lineChart.axisRight.isEnabled = false
            lineChart.animateX(2500)

            lineChart.xAxis.textColor = Color.WHITE
            lineChart.axisLeft.textColor = Color.WHITE
            lineChart.setViewPortOffsets(150F, 50F, 100F, 100F)

            lineChart.setDrawBorders(false);
        }
    }


    private fun getData(): LineData {

        val values = generateFullNightData()

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

    fun generateFullNightData(): ArrayList<Entry> {
        val values = ArrayList<Entry>()
        val startTime = Calendar.getInstance()
        startTime.set(2024, Calendar.MAY, 7, 22, 0) // Data e ora di inizio del sonno
        val endTime = Calendar.getInstance()
        endTime.set(2024, Calendar.MAY, 8, 6, 0) // Data e ora di fine del sonno

        val random = Random()

        val sleepPhaseDuration = 30 * 60 * 1000 // 30 minuti

        val currentTime = startTime.clone() as Calendar
        while (currentTime.before(endTime)) {
            val timestamp = currentTime.timeInMillis

            // Generazione casuale del tipo di sonno
            val sleepType = random.nextInt(4)

            // Aggiunta dei dati all'elenco di valori con etichetta
            val fTimestamp = timestamp.toFloat()
            val fSleepType = sleepType.toFloat()
            val newEntry = Entry(fTimestamp, fSleepType)
            println(newEntry)
            values.add(newEntry)

            // Avanzamento del tempo di una durata fissa per ogni fase del sonno
            currentTime.timeInMillis += sleepPhaseDuration
        }
        return values
    }
}

class SleepTimestampFormatter : ValueFormatter() {
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        return dateFormat.format(Date(value.toLong()))
    }
}