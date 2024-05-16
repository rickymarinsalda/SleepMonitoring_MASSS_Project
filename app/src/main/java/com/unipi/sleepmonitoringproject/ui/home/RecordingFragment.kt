package com.unipi.sleepmonitoringproject.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.unipi.sleepmonitoringproject.MainActivity
import com.unipi.sleepmonitoringproject.R
import com.unipi.sleepmonitoringproject.databinding.FragmentHomeBinding

class RecordingFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recording, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Hide toolbar and recording button */
        (activity as MainActivity).supportActionBar?.hide()
        (activity as MainActivity).binding.appBarMain.fab.hide()

        /* Set the listener for the stop button */
        val returnButton = view.findViewById<Button>(R.id.stop_button)
        returnButton.setOnClickListener {

            /* Show toolbar and recording button */
            (activity as MainActivity).supportActionBar?.show()
            (activity as MainActivity).binding.appBarMain.fab.show()

            /* Return to the home fragment */
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        /* Show toolbar and recording button */
        (activity as MainActivity).supportActionBar?.show()
        (activity as MainActivity).binding.appBarMain.fab.show()
    }
}