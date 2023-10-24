package com.footprint.app.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.footprint.app.R
import com.footprint.app.databinding.FragmentHomeFavoriteBinding


class HomeFavoriteFragment : Fragment(R.layout.fragment_home_favorite) {
    private var _binding: FragmentHomeFavoriteBinding? = null
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeFavoriteBinding.bind(view)
        binding.homeFavoriteRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
//        binding.homeFavoriteRecyclerView.adapter =
//            FavoriteAdapter(viewModel.commentitem.value ?: ArrayList<WalkModel>()) // bundle,argument로 넘겨서 이용해야할듯
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}