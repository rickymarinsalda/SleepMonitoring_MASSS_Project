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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var dataAvailable: Boolean = true

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        showLastNight(root)

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showLastNight(root: View) {
        // Prendo la box del line chart
        val constraintLayout: ConstraintLayout = root.findViewById(R.id.chart_parent)

        // Se ho registrato:
        // Metto titolo e sottotitolo
        if(dataAvailable) {

            /* Creation of the title */
            val lastNightTitle = TextView(context)
            lastNightTitle.id = R.id.lastNightTitle
            lastNightTitle.text = "Your last night of sleep"
            lastNightTitle.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            lastNightTitle.setTextColor(Color.rgb(174, 193, 232))
            lastNightTitle.textSize = 24f
            lastNightTitle.setTypeface(null, Typeface.BOLD)

            val customFont = ResourcesCompat.getFont(requireContext(), R.font.source_serif_pro)
            // Impostazione del font sulla TextView
            customFont?.let {
                lastNightTitle.typeface = it
            }

            constraintLayout.addView(lastNightTitle)

            /* Creation of the current date TextView */
            val currentDateTextView = TextView(context)
            currentDateTextView.id = R.id.currentDateTextView
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy") // Formato della data desiderato
            val currentDateText = currentDate.format(formatter)
            currentDateTextView.text = currentDateText
            currentDateTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            currentDateTextView.setTextColor(Color.rgb(174, 193, 232))
            currentDateTextView.textSize = 18f
            currentDateTextView.setTypeface(null, Typeface.BOLD)

            customFont?.let {
                currentDateTextView.typeface = it
            }

            // Aggiungo il TextView per la data corrente al ConstraintLayout
            constraintLayout.addView(currentDateTextView)

            // Creo il line chart
            val lineChart = SleepLineChart(root)

            // Imposto i vincoli per il titolo
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

            showDataFromLastNight(lineChart)
        }

        // Se non ho registrato:
        // Testo "Inizia a registrare"
    }

    private fun showDataFromLastNight(lineChart: SleepLineChart) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}