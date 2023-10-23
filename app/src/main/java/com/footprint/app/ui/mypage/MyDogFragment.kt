package com.footprint.app.ui.mypage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.footprint.app.R
import com.footprint.app.databinding.FragmentMyDogBinding

class MyDogFragment : Fragment(R.layout.fragment_my_dog) {

    private lateinit var viewModel: MyPageViewModel
    private var _binding: FragmentMyDogBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMyDogBinding.bind(view)

        viewModel = ViewModelProvider(requireActivity()).get(MyPageViewModel::class.java)

        setupCompleteButton()
    }

    private fun setupCompleteButton() {
        binding.dogComplete.setOnClickListener {
            val dogName = binding.dogNameword.text.toString()
            val dogAgeText = binding.dogAge.text.toString()
            val dogAge = dogAgeText.toIntOrNull() ?: 0

            val selectedRadioButtonId = binding.dogSexGroup.checkedRadioButtonId
            val dogSex = when (selectedRadioButtonId) {
                R.id.man -> "남아"
                R.id.woman -> "여아"
                else -> {
                    showToast("성별을 선택하세요.")
                    return@setOnClickListener
                }
            }


            // 개 정보를 뷰모델에 추가
            viewModel.addDog(dogName, dogAge, dogSex)

            // 마이 페이지로 이동
            findNavController().navigate(R.id.mypage)
        }
    }

    private fun showToast(message: String) {
        // 토스트 메시지 표시
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
