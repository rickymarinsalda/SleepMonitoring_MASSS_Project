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

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val galleryViewModel =
        //    ViewModelProvider(this).get(StatsViewModel::class.java)

        _binding = FragmentStatsBinding.inflate(inflater, container, false)

        val root: View = binding.root

        Log.d("DEBUG", "root -> $root")

        val barChart = SleepBarChart(context, null)

        binding.root.addView(barChart)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}