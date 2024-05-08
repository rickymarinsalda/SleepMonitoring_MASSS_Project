package com.unipi.sleepmonitoringproject.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.unipi.sleepmonitoringproject.databinding.FragmentHomeBinding
import com.unipi.sleepmonitoringproject.stats.SleepLineChart

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        createLineChart(root)

        return root
    }

    private fun createLineChart(root: View) {
        //TODO
        val lineChart = SleepLineChart(root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}