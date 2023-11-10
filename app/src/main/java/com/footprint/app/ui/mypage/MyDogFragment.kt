package com.footprint.app.ui.mypage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.footprint.app.Constants
import com.footprint.app.FirebaseDatabaseManager.uploadImage
import com.footprint.app.R
import com.footprint.app.api.model.PetInfoModel
import com.footprint.app.databinding.FragmentMyDogBinding
import com.footprint.app.ui.home.HomeViewModel

class MyDogFragment : Fragment(R.layout.fragment_my_dog) {

    private lateinit var viewModel: MyPageViewModel
    private var _binding: FragmentMyDogBinding? = null
    private var image: Uri? = null
    private val binding get() = _binding!!

    private val homeViewModel by activityViewModels<HomeViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMyDogBinding.bind(view)

        viewModel = ViewModelProvider(requireActivity()).get(MyPageViewModel::class.java)

        setupCompleteButton()
        setupCancelButton()
        binding.ivUploadImage.setOnClickListener {
            openGallery()
        }
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
            uploadImage(image) {
                val petInfoModel = PetInfoModel(
                    timestamp = System.currentTimeMillis(),
                    petImageUrl = it,
                    petName = dogName,
                    petAge = dogAge.toString(),
                    petSex = when (selectedRadioButtonId) {
                        R.id.man -> "남아"
                        R.id.woman -> "여아"
                        else -> ""
                    }
                )
                homeViewModel.updatePetInfo(petInfoModel)
                // 마이 페이지로 이동
                findNavController().navigate(R.id.mypage)
            }


        }
    }



    private fun showToast(message: String) {
        // 토스트 메시지 표시
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun openGallery() { // 갤러리를 여는 함수.
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES // READ_EXTERNAL_STORAGE 권한이 있는지 확인. 앱이 사용자의 저장소에서 파일을 읽을수 있도록 허용하는 권한. android.Manifest를 import 해와야한다.
            ) != PackageManager.PERMISSION_GRANTED // PackageManager.PERMISSION_GRANTED는 권한 승인상태. 승인상태가 아니라면, 아래의 실행문을 실행하게 된다.
        ) {
            Log.d("FootprintApp","openGallery1")
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                Constants.REQUEST_PERMISSION
            )
            // requestPermissions 메서드를 이용해 사용자에게 해당 권한을 요청함.
        } else { // 권한이 없는상태가 아니라면(= 권한이 있는 상태라면)
            Log.d("FootprintApp","openGallery2")
            val intent = Intent(Intent.ACTION_PICK) // 이미지를 선택할 수 있는 액션을 가진 인텐트 객체를 생성, 선택을 하면 intent에 데이터가 담김
            intent.type = "image/*" // 갤러리에서 이미지 파일만 표시하도록 지정
            startActivityForResult(intent, Constants.REQUEST_GALLERY)
            // REQUEST_GALLERY = 1이면, 갤러리 액티비티를 시작하면서 이 액티비티에서 결과를 반환받을 것임을 알린다.
        } // 이 함수의 결과로 데이터를 선택하면 intent에 이미지 데이터(자료형은 Uri)가 담김
    }
    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.REQUEST_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("FootprintApp","onRequestPermissionsResult1")
                    openGallery()
                } else {
                    // 권한 거부 처리
                    Toast.makeText(requireContext(), "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                    Log.d("FootprintApp","onRequestPermissionsResult2")

                }
            }
        }
    }
    // 이미지 선택 결과 처리
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_GALLERY && resultCode == AppCompatActivity.RESULT_OK) {
            data?.data.let {
                image = it
                Glide.with(requireContext())
                    .load(it)
                    .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                    .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                    .into(binding.ivUploadImage)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
