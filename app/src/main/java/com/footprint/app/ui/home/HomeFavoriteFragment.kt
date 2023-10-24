package com.footprint.app.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.footprint.app.R
import com.footprint.app.databinding.FragmentHomeFavoriteBinding
import com.footprint.app.util.ItemClick


class HomeFavoriteFragment : Fragment(R.layout.fragment_home_favorite) {
    private var _binding: FragmentHomeFavoriteBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeFavoriteBinding.bind(view)
        binding.homeFavoriteRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.homeFavoriteRecyclerView.adapter =
            FavoriteAdapter(requireContext(),homeViewModel.walkList).apply{
            itemClick = object : ItemClick {
                override fun onClick(view: View, position: Int) {
                    Log.d("FootprintApp","클릭은잘되용")
                    val bundle = Bundle()
                    bundle.putInt("position", position)
                    findNavController().navigate(R.id.homefavoriteitem, bundle)
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}