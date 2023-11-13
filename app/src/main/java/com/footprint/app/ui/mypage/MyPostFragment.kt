package com.footprint.app.ui.mypage
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.footprint.app.FirebaseDatabaseManager.readMyPostdata
import com.footprint.app.R
import com.footprint.app.databinding.FragmentMyPostBinding
import com.footprint.app.databinding.FragmentMyWalkBinding
import com.footprint.app.databinding.FragmentMypageBinding
import com.footprint.app.ui.community.CommunityAdapter
import com.footprint.app.ui.home.FavoriteAdapter
import com.footprint.app.ui.home.HomeViewModel
import com.footprint.app.util.ItemClick
import com.google.firebase.auth.FirebaseAuth

class MyPostFragment : Fragment(R.layout.fragment_my_post) {

    private var _binding: FragmentMyPostBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val myPageViewModel by activityViewModels<MyPageViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMyPostBinding.bind(view)

        backPage()
        recyclerview()
    }

    private fun backPage(){
        binding.postPageBack.setOnClickListener {
            findNavController().navigate(R.id.mypage)
        }
    }
    private fun recyclerview(){
        if (homeViewModel.walkList.value?.isEmpty()!!) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.ivEmpty.visibility = View.VISIBLE
        }
        binding.rvMyPost.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvMyPost.adapter = CommunityAdapter(requireContext(), homeViewModel.myPostList.value!!).apply {
            itemClick = object : ItemClick {
                override fun onClick(view: View, position: Int) {
//                    if (findNavController().currentDestination?.id == R.id.navigation_community) {
//                        val bundle = Bundle().apply {
//                            communityViewModel.currentPostposition = position
//                            putInt("position", position)
//                            putString("postKey,", communityViewModel.postList.value!![position].postKey)
//                        }
//                        findNavController().navigate(R.id.communityPost, bundle)
//
//                    }
                }
            }
        }
    }
}
