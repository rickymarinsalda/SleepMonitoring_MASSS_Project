package com.unipi.sleepmonitoringproject.ui.home

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import android.animation.ValueAnimator
import android.widget.LinearLayout

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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
            lastNightTitle.setTextColor(Color.rgb(174, 193, 232))
            lastNightTitle.textSize = 24f
            lastNightTitle.setTypeface(null, Typeface.BOLD)

            val customFont = ResourcesCompat.getFont(requireContext(), R.font.source_code_pro)
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
            currentDateTextView.textSize = 18f
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

            showDataFromLastNight(lineChart)
        }

        // TODO
        // Se non ho registrato:
        // Testo "Inizia a registrare"
    }

    private fun showDataFromLastNight(lineChart: SleepLineChart) {
        val horizontalView: LinearLayout = root.findViewById(R.id.data_list)

        /* Total time in bed */
        // Calculate the total time slept
        val startTime = lineChart.getStartTime().timeInMillis
        val endTime = lineChart.getEndTime().timeInMillis
        val totTime = (endTime - startTime)/3600000.0

        val totTimeTextView = createSleepDataElem(totTime, R.string.time_in_bed)

        horizontalView.addView(totTimeTextView)

        /* Time to fall asleep */

        /* Total time in deep sleep */

        /* Total time in light sleep */

        /* Total time in REM phase */

        /* Total time awake */

        /* Overall quality */
    }

    private fun createSleepDataElem(numericData: Double, stringType: Int): TextView {
        // Show the total time slept
        val sleepDataElem = TextView(context)
        val formattedText = getString(stringType, numericData)
        sleepDataElem.text = formattedText
        sleepDataElem.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
        sleepDataElem.setTextColor(Color.rgb(174, 193, 232))
        sleepDataElem.textSize = 20f

        val padding = resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin)
        sleepDataElem.setPaddingRelative(padding, sleepDataElem.paddingTop, sleepDataElem.paddingEnd, sleepDataElem.paddingBottom)

        val customFont = ResourcesCompat.getFont(requireContext(), R.font.source_code_pro)
        customFont?.let {
            sleepDataElem.typeface = it
        }
        applyAnimation(sleepDataElem)

        return sleepDataElem
    }

    private fun applyAnimation(textToAnimate: TextView) {
        val animationDuration = 150

        val textToType = textToAnimate.text
        val animator = ValueAnimator.ofInt(0, textToType.length)
        animator.duration = (animationDuration * textToType.length).toLong()
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            val animatedText = if (progress >= textToType.length) textToType else textToType.substring(0, progress + 1)
            textToAnimate.text = animatedText
        }
        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}