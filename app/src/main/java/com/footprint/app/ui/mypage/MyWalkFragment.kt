package com.footprint.app.ui.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.footprint.app.R
import com.footprint.app.databinding.FragmentMyWalkBinding
import com.footprint.app.databinding.FragmentMypageBinding

class MyWalkFragment : Fragment(R.layout.fragment_my_walk) {

    private var _binding: FragmentMyWalkBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMyWalkBinding.bind(view)

        backPage()

    }
    private fun backPage(){
        binding.walkPageBack.setOnClickListener {
            findNavController().navigate(R.id.mypage)
        }
    }
}