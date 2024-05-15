package com.unipi.sleepmonitoringproject.stats

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.unipi.sleepmonitoringproject.R


class SleepPieChart(rootView: View) {

    val id: Int = R.id.pie_chart
    private val pieChart: PieChart = rootView.findViewById(R.id.pie_chart)

    init {

        setupChart(rootView)

        setData()
    }

    private fun setupChart(root: Any?) {

        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        pieChart.dragDecelerationFrictionCoef = 0.95f

        pieChart.setCenterTextTypeface(Typeface.DEFAULT)

        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)

        pieChart.setTransparentCircleAlpha(110)
        pieChart.setTransparentCircleColor(Color.WHITE)

        pieChart.setHoleRadius(58f)
        pieChart.setTransparentCircleRadius(61f)

        pieChart.setDrawCenterText(true)

        pieChart.setRotationAngle(0f)
        // enable rotation of the chart by touch
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true

        pieChart.setOnChartValueSelectedListener(null)

        pieChart.animateY(1400, Easing.EaseInOutQuad)

        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD)
        pieChart.setEntryLabelTextSize(12f)
    }

    private fun setData() {
        val entries = ArrayList<PieEntry>()

        for (i in 0 until 4) {
            entries.add(
                PieEntry(
                    (Math.random() + 1).toFloat(),
                    if (i == 0) "Deep Sleep" else if (i == 1) "Light Sleep" else if (i == 2) "REM Sleep" else "Awake"
                )
            )
        }

        val dataSet = PieDataSet(entries, "Election Results")

        dataSet.setDrawIcons(false)

        dataSet.setSliceSpace(3f)
        dataSet.setIconsOffset(MPPointF(0f, 40f))
        dataSet.selectionShift = 5f

        // add a lot of colors
        val colors = ArrayList<Int>()

        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)

        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)

        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)

        colors.add(ColorTemplate.getHoloBlue())

        dataSet.colors = colors
        //dataSet.setSelectionShift(0f);

        //dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        val mTf: Typeface = Typeface.DEFAULT
        data.setValueTypeface(mTf)
        pieChart.data = data

        // undo all highlights
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }
}
