package com.footprint.app.ui.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.footprint.app.R
import com.footprint.app.databinding.FragmentMyWalkBinding
import com.footprint.app.databinding.FragmentMypageBinding
import com.footprint.app.ui.home.FavoriteAdapter
import com.footprint.app.ui.home.HomeViewModel
import com.footprint.app.util.ItemClick

class MyWalkFragment : Fragment(R.layout.fragment_my_walk) {

    private var _binding: FragmentMyWalkBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel by activityViewModels<HomeViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMyWalkBinding.bind(view)

        backPage()
        recyclerview()
    }
    private fun backPage(){
        binding.walkPageBack.setOnClickListener {
            findNavController().navigate(R.id.mypage)
        }
    }
    private fun recyclerview(){
        if (homeViewModel.walkList.value?.isEmpty()!!) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.ivEmpty.visibility = View.VISIBLE
        }
        binding.rvMyWalk.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvMyWalk.adapter =
            FavoriteAdapter(requireContext(), homeViewModel.walkList.value!!).apply {
                itemClick = object : ItemClick {
                    override fun onClick(view: View, position: Int) {
                        val bundle = Bundle()
                        bundle.putInt("position", position)
                        findNavController().navigate(R.id.navigation_myWalkFavoriteItem, bundle)
                    }
                }
            }
    }
}