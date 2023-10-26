package com.footprint.app.ui.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.footprint.app.R
import com.footprint.app.databinding.FragmentCommunityBinding
import com.footprint.app.databinding.FragmentCommunityPlusBinding


class Community_plusFragment : Fragment(R.layout.fragment_community_plus) {

    private var _binding: FragmentCommunityPlusBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCommunityPlusBinding.bind(view)

        goCommunityPage()

    }

    private fun goCommunityPage(){
        binding.communityplusComplete.setOnClickListener {
            findNavController().navigate(R.id.community)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
}
}