package com.unipi.sleepmonitoringproject.ui.stats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.unipi.sleepmonitoringproject.databinding.FragmentStatsBinding
import com.unipi.sleepmonitoringproject.stats.SleepBarChart

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)

        val root: View = binding.root
        Log.d("DEBUG", "root -> $root")

        // Assuming bar_chart.xml is a layout for SleepBarChart and
        // we are inflating it programmatically.
        val barChart = SleepBarChart(context, null)
        // Assuming there is a container or a placeholder in fragment_stats.xml to add the bar chart.
        binding.barChartContainer.addView(barChart)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
