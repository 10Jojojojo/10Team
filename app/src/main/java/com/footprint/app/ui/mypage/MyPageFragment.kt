package com.footprint.app.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.footprint.app.R
import com.footprint.app.databinding.FragmentCommunityBinding
import com.footprint.app.databinding.FragmentMypageBinding
import com.footprint.app.ui.community.CommunityViewModel
import com.footprint.app.ui.mypage_revise.MyPage_reviseFragment

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMypageBinding.bind(view)
        revisepage()
    }
    private fun revisepage(){
        binding.mypageRevise.setOnClickListener {
            findNavController().navigate(R.id.mypage)
        }
    }
}