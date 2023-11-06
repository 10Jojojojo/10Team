package com.footprint.app.ui.mypage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.footprint.app.R
import com.footprint.app.databinding.FragmentMypageBinding
import kotlin.math.log

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

        nextPage()

        uesrInfo()

        dog()

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