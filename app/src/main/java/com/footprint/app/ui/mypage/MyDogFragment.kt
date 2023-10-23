package com.footprint.app.ui.mypage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
        setupCancelButton()
    }


    private fun setupCancelButton() {
        binding.cance.setOnClickListener {
            findNavController().navigate(R.id.mypage)
        }
    }

    private fun setupCompleteButton() {
        binding.dogComplete.setOnClickListener {
            val dogName = binding.dogplusNameword.text.toString()
            val dogAgeText = binding.dogplusAge.text.toString()
            val dogAge = dogAgeText.toIntOrNull() ?: 0

            //이름 또는 나이 둘다 입력해야 통과
            if (dogName.isEmpty() || dogAge == 0){
                showToast("정보를 모두 입력해주세요")
                return@setOnClickListener
            }

            //이름 유효성 검사
            val namePattern = "^[a-zA-Z가-힣\\s]*$".toRegex()
            if (!namePattern.matches(dogName)) {
                showToast("이름에 특수 문자 또는 잘못된 문자가 포함되어 있습니다. 다시 입력하세요.")
                return@setOnClickListener
            }

            //성별 설정하지 않을시 다시 선택하게 함
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
