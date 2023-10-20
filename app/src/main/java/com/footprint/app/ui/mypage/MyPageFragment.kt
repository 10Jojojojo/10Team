package com.footprint.app.ui.mypage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.footprint.app.R
import com.footprint.app.databinding.FragmentMypageBinding

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    //뷰모델과 연동
    private lateinit var viewModel : MyPageViewModel

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        _binding = FragmentMypageBinding.bind(view)

        //뷰모델 초기화
        viewModel = ViewModelProvider(requireActivity()).get(MyPageViewModel::class.java)

        revisepage()
        uesrInfo()

    }

    // 마이페이지에서 수정 페이지로 이동
    private fun revisepage(){
        binding.mypageRevise.setOnClickListener {
            findNavController().navigate(R.id.mypage)

        }
    }

    private fun uesrInfo(){

        //// ViewModel에서 사용자 정보 가져오기
        val name = viewModel.name
        val introduction = viewModel.introduction
        val town = viewModel.town

        // 사용자 정보 표시
        binding.mypageName.text = name
        binding.mypageIntroduction.text = introduction
        binding.town.text = town
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}