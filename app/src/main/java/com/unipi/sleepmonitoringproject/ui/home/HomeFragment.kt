package com.unipi.sleepmonitoringproject.ui.home

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.unipi.sleepmonitoringproject.R
import com.unipi.sleepmonitoringproject.databinding.FragmentHomeBinding
import com.unipi.sleepmonitoringproject.stats.SleepLineChart
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.unipi.sleepmonitoringproject.stats.SleepPieChart
import java.util.LinkedList
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private var dataAvailable: Boolean = true

    private lateinit var root: View

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        root = binding.root

        showLastNight()

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showLastNight() {
        val constraintLayout: ConstraintLayout = root.findViewById(R.id.chart_parent)

        /* If the user collected some data */
        if(dataAvailable) {

            /* Creation of the title */
            val lastNightTitle = TextView(context)
            lastNightTitle.id = R.id.lastNightTitle
            lastNightTitle.text = getString(R.string.home_title_after_rec)
            lastNightTitle.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            lastNightTitle.setTextColor(Color.WHITE)
            lastNightTitle.textSize = 30f
            lastNightTitle.setTypeface(null, Typeface.BOLD)

            val customFont = ResourcesCompat.getFont(requireContext(), R.font.source_serif_pro)
            customFont?.let {
                lastNightTitle.typeface = it
            }

            constraintLayout.addView(lastNightTitle)

            /* Creation of the current date TextView */
            val currentDateTextView = TextView(context)
            currentDateTextView.id = R.id.currentDateTextView
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
            val currentDateText = currentDate.format(formatter)
            currentDateTextView.text = currentDateText
            currentDateTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            currentDateTextView.setTextColor(Color.rgb(174, 193, 232))
            currentDateTextView.textSize = 20f
            currentDateTextView.setTypeface(null, Typeface.BOLD)

            customFont?.let {
                currentDateTextView.typeface = it
            }

            constraintLayout.addView(currentDateTextView)

            /* Apply the constraint set to title and date TextViews */
            val lastNightTitleLayoutParams = lastNightTitle.layoutParams as ConstraintLayout.LayoutParams
            lastNightTitleLayoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            lastNightTitleLayoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT

            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)
            constraintSet.connect(lastNightTitle.id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP)
            constraintSet.connect(lastNightTitle.id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START)
            constraintSet.connect(lastNightTitle.id, ConstraintSet.END, constraintLayout.id, ConstraintSet.END)
            constraintSet.setHorizontalBias(lastNightTitle.id, 0.5f)
            constraintSet.setVerticalBias(lastNightTitle.id, 0.3f)
            constraintSet.applyTo(constraintLayout)

            val currentDateLayoutParams = currentDateTextView.layoutParams as ConstraintLayout.LayoutParams
            currentDateLayoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            currentDateLayoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT

            constraintSet.clone(constraintLayout)
            constraintSet.connect(currentDateTextView.id, ConstraintSet.TOP, lastNightTitle.id, ConstraintSet.BOTTOM)
            constraintSet.connect(currentDateTextView.id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START)
            constraintSet.connect(currentDateTextView.id, ConstraintSet.END, constraintLayout.id, ConstraintSet.END)
            constraintSet.setHorizontalBias(currentDateTextView.id, 0.5f)
            constraintSet.setVerticalBias(currentDateTextView.id, 0.4f)
            constraintSet.applyTo(constraintLayout)

            /* Creation of the sleep line chart */
            val lineChart = SleepLineChart(root)

            /* Set sleep informations views as visible */
            val sleepInfoLayout: LinearLayout = root.findViewById(R.id.sleep_info_layout)
            sleepInfoLayout.visibility = View.VISIBLE

            /* Total time in bed */
            val startTime = lineChart.getStartTime().timeInMillis
            val endTime = lineChart.getEndTime().timeInMillis
            val totTime = (endTime - startTime)/3600000.0
            val totTimeTextView: TextView = root.findViewById(R.id.time_in_bed)
            totTimeTextView.text = getString(R.string.time_asleep, totTime)

            /* Time to fall asleep */
            val startTimeAsleep = lineChart.getStartTimeAsleep()
            val differenceInMillis = startTimeAsleep - startTime
            val totTimeToFallAsleep = ((differenceInMillis / (1000.0 * 60.0)) * 10.0).roundToInt() / 10.0
            val timeToFallAsleepTextView: TextView = root.findViewById(R.id.time_to_fall_asleep)
            timeToFallAsleepTextView.text = getString(R.string.to_fall_asleep, totTimeToFallAsleep)

            /* Creation of the pie chart */
            SleepPieChart(root)
        }
        else {
            val noDataTextView = TextView(context)
            noDataTextView.id = R.id.noDataTextView
            noDataTextView.text = getString(R.string.home_title_before_rec)
            noDataTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            noDataTextView.setTextColor(Color.WHITE)
            noDataTextView.textSize = 30f
            noDataTextView.setTypeface(null, Typeface.BOLD)

            val customFont = ResourcesCompat.getFont(requireContext(), R.font.source_serif_pro)
            customFont?.let {
                noDataTextView.typeface = it
            }

            constraintLayout.addView(noDataTextView)

            val noDataLayoutParams = noDataTextView.layoutParams as ConstraintLayout.LayoutParams
            noDataLayoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            noDataLayoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT

            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)
            constraintSet.connect(noDataTextView.id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP)
            constraintSet.connect(noDataTextView.id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START)
            constraintSet.connect(noDataTextView.id, ConstraintSet.END, constraintLayout.id, ConstraintSet.END)
            constraintSet.setHorizontalBias(noDataTextView.id, 0.5f)
            constraintSet.setVerticalBias(noDataTextView.id, 0.3f)
            constraintSet.applyTo(constraintLayout)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}