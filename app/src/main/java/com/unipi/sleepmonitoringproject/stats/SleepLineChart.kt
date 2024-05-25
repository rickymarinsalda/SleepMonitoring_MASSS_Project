package com.unipi.sleepmonitoringproject.stats

import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.unipi.sleepmonitoring_masss_library.ClassifierML
import com.unipi.sleepmonitoring_masss_library.DbLoader
import com.unipi.sleepmonitoring_masss_library.TimeSeries
import com.unipi.sleepmonitoring_masss_library.classifySeries
import com.unipi.sleepmonitoringproject.MainActivity
import com.unipi.sleepmonitoringproject.R
import com.unipi.sleepmonitoring_masss_library.db.EventManagerDbHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.Calendar.*
import kotlin.coroutines.coroutineContext

class SleepLineChart(val rootView: View, val lastNightData: TimeSeries) {

    val id: Int = R.id.line_chart
    private val lineChart: LineChart = rootView.findViewById(R.id.line_chart)
    private lateinit var startTime : Calendar
    private lateinit var endTime : Calendar
    private var startTimeAsleep: Long = -1
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

            lineChart.legend.isEnabled = false

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

            lineChart.setDrawBorders(false)
        }
    }


    private fun getData(): LineData {

        val values = generateFullNightData()
        calculateSleepTotals(values)

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
            sleepTotals[sleepType] = sleepDuration + 0.5 // Incrementa la durata di ciascun tipo di sonno di 0.5 (30 minuti)
        }

        deepSleepTotal = sleepTotals[0] ?: 0.0
        remSleepTotal = sleepTotals[1] ?: 0.0
        lightSleepTotal = sleepTotals[2] ?: 0.0
        awakeTotal = sleepTotals[3] ?: 0.0
    }

    private fun generateFullNightData(): ArrayList<Entry> {
        val values = ArrayList<Entry>()

        /*
        Codice aggiunto per prendere dal db + alg + dati
*/
        val classifier = ClassifierML(rootView.context)

        val res = classifySeries(classifier,lastNightData)
        startTime = getInstance()
        endTime = getInstance()
        endTime.timeInMillis = lastNightData.data[0].timestamp
        startTime.timeInMillis = lastNightData.data[0].timestamp
        val startTimestamp = startTime.clone() as Calendar


        for(i in res.indices){
            startTimestamp.add(MINUTE, 10) // Aggiunge 10 minuti al timestamp
            values.add(Entry(startTimestamp.timeInMillis.toFloat(), res[i].toFloat()))
        }
        endTime = startTimestamp
        return values

        /*
        -------------------------------------------------
        */



/*
        val random = Random()
        startTime = getInstance()
        startTime.set(2024, MAY, 24, 22, 0) // Data e ora di inizio del sonno
        endTime = getInstance()
        endTime.set(2024, MAY, 26, 6, 0) // Data e ora di fine del sonno
        val sleepPhaseDuration = 30 * 60 * 1000 // 30 minuti

        val currentTime = startTime.clone() as Calendar
        while (currentTime.before(endTime)) {
            val timestamp = currentTime.timeInMillis

            // Generazione casuale del tipo di sonno
            val sleepType = random.nextInt(4)

            // Se il tipo di sonno non è "awake", registra il timestamp del primo dato non "awake"
            if (sleepType == 3 && startTimeAsleep.toInt() == -1) {
                startTimeAsleep = timestamp
            }

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
*/

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

class SleepTimestampFormatter : ValueFormatter() {
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        return dateFormat.format(Date(value.toLong()))
    }
}

/*
      Codice aggiunto per prendere dal db + alg + dati
*/

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



/*
-------------------------------------------------
*/