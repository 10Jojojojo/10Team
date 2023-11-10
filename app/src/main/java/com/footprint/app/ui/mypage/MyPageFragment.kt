package com.footprint.app.ui.mypage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.footprint.app.FirebaseDatabaseManager.savePetInfoDataActive
import com.footprint.app.R
import com.footprint.app.api.model.ProfileModel
import com.footprint.app.databinding.FragmentMypageBinding
import kotlin.math.log
import com.footprint.app.ui.home.HomeViewModel
import com.footprint.app.util.ItemClick

import kotlin.math.log
import androidx.lifecycle.ViewModelProvider

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var dogAdapter: DogAdapter

    private val homeViewModel by activityViewModels<HomeViewModel>()

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        _binding = FragmentMypageBinding.bind(view)

        nextPage()
        dog()
        observeViewModel()

    }
    private fun observeViewModel() {
        homeViewModel.profile.observe(viewLifecycleOwner){
            if(it != ProfileModel()){
                Glide.with(requireContext())
                    .load(it.profileImageUri ?: R.drawable.ic_mypage_black_24) // selectedImageUri에 저장된 이미지 URL
                    .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                    .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                    .into(binding.profileImage) // 해당 이미지를 표시할 ImageView
                binding.mypageName.text = homeViewModel.profile.value?.nickName ?: "이름"
                binding.town.text = homeViewModel.profile.value?.address ?: "주소"
                binding.mypageIntroduction.text = homeViewModel.profile.value?.introduction ?: "자기소개"
            }}
        homeViewModel.petInfoList.observe(viewLifecycleOwner){
            if(dogAdapter!=null) dogAdapter.notifyDataSetChanged()
//            viewModel.updatePetInfo(it.last()) // 나중에 수정하기. 펫인포는 마지막이아니라 고른것을 삭제해야한다.
        }
    }
    // 마이페이지에서 수정 페이지로 이동
    private fun nextPage(){
        binding.mypageRevise.setOnClickListener {
            findNavController().navigate(R.id.mypage_revise)

        }
        binding.myDogPlus.setOnClickListener {
            findNavController().navigate(R.id.dog)
        }
        binding.postLinearLayout.setOnClickListener{
            findNavController().navigate(R.id.Post)
            Log.d("layout","post")
        }
        binding.walkLinearLayout.setOnClickListener {
            findNavController().navigate(R.id.walk)
            Log.d("layout","walk")
        }
    }

    private fun dog() {
        var recyclerView = binding.dogCard
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        // 어댑터 초기화 및 dogList와 onDeleteClickListener를 전달
        dogAdapter = DogAdapter(homeViewModel.petInfoList){}.apply {
            itemClick = object : ItemClick {
                override fun onClick(view: View, position: Int) {
                    homeViewModel.petInfoList.value?.get(position)?.activePet = !homeViewModel.petInfoList.value?.get(position)?.activePet!!
                    savePetInfoDataActive(homeViewModel.petInfoList.value?.get(position)?.timestamp!!){
                        notifyDataSetChanged()
                    }
                }
            }
        }

        recyclerView.adapter = dogAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}