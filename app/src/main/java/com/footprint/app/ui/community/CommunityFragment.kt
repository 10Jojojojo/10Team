package com.footprint.app.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.footprint.app.R
import com.footprint.app.databinding.FragmentCommunityBinding
import kotlin.math.log

class CommunityFragment : Fragment(R.layout.fragment_community) {

    private var _binding: FragmentCommunityBinding? = null

    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCommunityBinding.bind(view)

        goCommunityplusPage()
    }

    private fun goCommunityplusPage() {
        binding.communityPen.setOnClickListener {
            findNavController().navigate(R.id.communityPlus)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}