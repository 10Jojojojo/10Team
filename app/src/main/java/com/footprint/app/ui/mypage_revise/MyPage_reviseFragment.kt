package com.footprint.app.ui.mypage_revise

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.footprint.app.R
import com.footprint.app.databinding.FragmentMyPageReviseBinding
import com.footprint.app.databinding.FragmentMypageBinding

class MyPage_reviseFragment : Fragment(R.layout.fragment_my_page_revise) {

    private var _binding: FragmentMyPageReviseBinding? = null
    private lateinit var callback : OnBackPressedCallback
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyPageReviseBinding.bind(view)



    }

}