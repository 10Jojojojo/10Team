package com.footprint.app.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.footprint.app.R
import com.footprint.app.databinding.FragmentHomeStopBinding


class HomeStopFragment : Fragment(R.layout.fragment_home_stop) {
    private var _binding: FragmentHomeStopBinding? = null
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeStopBinding.bind(view)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}