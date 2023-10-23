package com.footprint.app.ui.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.footprint.app.R
import com.footprint.app.databinding.FragmentMyPageReviseBinding

class MyPage_reviseFragment : Fragment(R.layout.fragment_my_page_revise) {

    //뷰모델과 연동
    private lateinit var viewModel : MyPageViewModel

    private var _binding: FragmentMyPageReviseBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyPageReviseBinding.bind(view)

        // ViewModel 초기화
        viewModel = ViewModelProvider(requireActivity()).get(MyPageViewModel::class.java)

        reUesrInfo()
        revisepage()
        cance()
        dog()
    }

    private fun reUesrInfo() {
        binding.save.setOnClickListener {

            // 사용자가 수정한 내용을 ViewModel에 저장
            val newName = binding.mypageReviseName.text.toString()
            val newIntroduction = binding.mypageReviseIntroduction.text.toString()
            val newTown = binding.town.text.toString()

            viewModel.name = newName
            viewModel.introduction = newIntroduction
            viewModel.town = newTown

            // 마이 페이지로 이동
            findNavController().navigate(R.id.mypage)
        }
    }

    private fun revisepage(){

        // 수정 페이지에서 ViewModel의 데이터를 화면에 표시
        binding.mypageReviseName.setText(viewModel.name)
        binding.mypageReviseIntroduction.setText(viewModel.introduction)
        binding.town.setText(viewModel.town)
    }

    private fun cance(){
        binding.Cance.setOnClickListener {
            findNavController().navigate(R.id.mypage)
        }
    }

    private fun dog(){
        var recyclerView = binding.dogCard
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        // 어댑터 초기화 및 dogList를 전달
        var adapter = DogAdapter(viewModel.dogList)
        recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
