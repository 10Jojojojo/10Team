package com.footprint.app.ui.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.footprint.app.Constants.STATUS_LOADED
import com.footprint.app.Constants.STATUS_NOT_LOADED
import com.footprint.app.FirebaseDatabaseManager
import com.footprint.app.FirebaseDatabaseManager.readPostList
import com.footprint.app.R
import com.footprint.app.databinding.FragmentCommunityBinding
import com.footprint.app.util.ItemClick
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class CommunityFragment : Fragment(R.layout.fragment_community) {

    private var _binding: FragmentCommunityBinding? = null

    private val binding get() = _binding!!

    //    private val communityViewModel by lazy { ViewModelProvider(this)[CommunityViewModel::class.java] }
    // 다른 Fragment 에서도 homeViewModel instance 를 참조 하기 위해 수정
    private val communityViewModel by activityViewModels<CommunityViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCommunityBinding.bind(view)
        initView()
        loadData()
    }

    private fun initView() {
        binding.rvTag.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTag.adapter =
            CommunityAdapter(requireContext(), communityViewModel.dummyTag).apply {
                itemClick = object : ItemClick {
                    override fun onClick(view: View, position: Int) {
                    }
                }
            }
        binding.rvPost.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvPost.adapter = CommunityAdapter(requireContext(), communityViewModel.post).apply {
            itemClick = object : ItemClick {
                override fun onClick(view: View, position: Int) {
                    if (findNavController().currentDestination?.id == R.id.navigation_community) {
                        val gson = Gson()
                        val post = communityViewModel.post[position]
                        val postJson = gson.toJson(post)
                        val bundle = Bundle().apply {
                            putString("post", postJson)
                        }
                        findNavController().navigate(R.id.communityPost, bundle)
                    }
                }
            }
        }
        binding.ivPen.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.navigation_community) {
                findNavController().navigate(R.id.communityPlus)
            } // 네비게이션 컴포넌트의 오류
            // 그 이유는 화살표로 어디로 가는지 다 정해놧는데, 컴터 입장에서 그걸 수행하기까지 시간이 걸림?
            // 예를들어 커뮤니티에서 커뮤니티 플러스로 가야하는데, 그 전에 내가 홈으로 가버리면 홈에서 커뮤니티 플러스를 가는게 되버려서 에러가 남
            // 커뮤니티 플러스 프래그먼트로 갈 때 액티비티의 바텀 네비게이션바를 안보이도록 하게 하고, 툴바?상단바? 의 좌측 영역에 뒤로가기 버튼을 만든다.
            //
        }
    }

    private fun loadData() {
        if (communityViewModel.postState == STATUS_NOT_LOADED) {
            readPostList(communityViewModel.post) {
                // 여기서 UI 갱신
                communityViewModel.postState = STATUS_LOADED
                binding.rvPost.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}