package com.footprint.app.ui.mypage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.footprint.app.FirebaseDatabaseManager.readProfile
import com.footprint.app.R
import com.footprint.app.databinding.FragmentMypageBinding
import com.footprint.app.ui.home.HomeViewModel

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    //뷰모델과 연동
    private lateinit var viewModel : MyPageViewModel

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel by activityViewModels<HomeViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        _binding = FragmentMypageBinding.bind(view)

        //뷰모델 초기화
        viewModel = ViewModelProvider(requireActivity()).get(MyPageViewModel::class.java)

        nextPage()

        uesrInfo()

        dog()
        readProfile("-NiUywbbHbsxPZrYtRZF") {
            it?.let {
                // 이미지 URL이 있는 경우에만 Glide를 사용하여 로드
                Glide.with(requireContext())
                    .load(it.selectedImageUri) // selectedImageUri에 저장된 이미지 URL
                    .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                    .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                    .into(binding.profileImage) // 해당 이미지를 표시할 ImageView
                homeViewModel.flagList= it.markerList.toMutableList()
            } ?: run {
                // profileModel이 null인 경우, 즉 데이터 로드 실패 또는 해당 프로필이 존재하지 않는 경우 처리
                Toast.makeText(requireContext(), "프로필을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
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

    }




    private fun uesrInfo(){

        // ViewModel에서 사용자 정보 가져오기
        val name = viewModel.name
        val introduction = viewModel.introduction
        val town = viewModel.town

        // 사용자 정보 표시
        binding.mypageName.text = name
        binding.mypageIntroduction.text = introduction
        binding.town.text = town
    }


    private fun dog() {
        var recyclerView = binding.dogCard
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        // 어댑터 초기화 및 dogList와 onDeleteClickListener를 전달
        val adapter = DogAdapter(viewModel.dogList){}

        recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}