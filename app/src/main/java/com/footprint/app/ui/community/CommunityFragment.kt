package com.footprint.app.ui.community

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.footprint.app.R
import com.footprint.app.databinding.FragmentCommunityBinding
import com.footprint.app.util.ItemClick

class CommunityFragment : Fragment(R.layout.fragment_community) {

    private var _binding: FragmentCommunityBinding? = null

    private val binding get() = _binding!!
    private val communityViewModel by lazy { ViewModelProvider(this)[CommunityViewModel::class.java] }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCommunityBinding.bind(view)
        initView()

    }
    private fun initView(){
        binding.rvTag.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTag.adapter =
            CommunityAdapter(requireContext(), communityViewModel.dummyTag as List<Any>).apply {
                itemClick = object : ItemClick {
                    override fun onClick(view: View, position: Int) {
                    }
                }
            }
        binding.rvPost.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvPost.adapter =
            CommunityAdapter(requireContext(), communityViewModel.dummyPost as List<Any>).apply {
                itemClick = object : ItemClick {
                    override fun onClick(view: View, position: Int) {
                    }
                }
            }
        binding.ivPen.setOnClickListener {
            findNavController().navigate(R.id.communityPlus)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}