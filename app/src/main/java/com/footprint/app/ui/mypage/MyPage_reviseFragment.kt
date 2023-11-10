package com.footprint.app.ui.mypage

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.footprint.app.Constants.REQUEST_GALLERY
import com.footprint.app.Constants.REQUEST_PERMISSION
import com.footprint.app.FirebaseDatabaseManager.uploadImage
import com.footprint.app.R
import com.footprint.app.api.model.ProfileModel
import com.footprint.app.databinding.FragmentMyPageReviseBinding
import com.footprint.app.ui.home.HomeViewModel


class MyPage_reviseFragment : Fragment(R.layout.fragment_my_page_revise) {
    private lateinit var viewModel: MyPageViewModel
    private var _binding: FragmentMyPageReviseBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    private val homeViewModel by activityViewModels<HomeViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyPageReviseBinding.bind(view)

        viewModel = ViewModelProvider(requireActivity()).get(MyPageViewModel::class.java)
        binding.profileImage.setOnClickListener {
            openGallery()
        }
        reUesrInfo()
        cance()
        dog()
        initView()
    }

    private fun initView() {
        homeViewModel.profile.value?.let {
            if (it != ProfileModel()) {
                Glide.with(requireContext())
                    .load(
                        it.profileImageUri ?: R.drawable.ic_mypage_black_24
                    ) // selectedImageUri에 저장된 이미지 URL
                    .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                    .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                    .into(binding.profileImage) // 해당 이미지를 표시할 ImageView
                binding.mypageReviseName.setText(it.nickName ?: "이름")
                binding.town.setText(it.address ?: "주소")
                binding.mypageReviseIntroduction.setText(it.introduction ?: "자기소개")
            }
        }
    }

    private fun reUesrInfo() {
        binding.save.setOnClickListener {
            if (imageUri != null) {
                uploadImage(imageUri) {
                    val profileModel = ProfileModel(
                        nickName = binding.mypageReviseName.text.toString(),
                        profileImageUri = it,
                        address = binding.town.text.toString(),
                        introduction = binding.mypageReviseIntroduction.text.toString()
                    )
                    homeViewModel.updateProfile(profileModel)
                    findNavController().navigate(R.id.mypage)
                }
            } else if (homeViewModel.profile.value?.profileImageUri != null) {
                val profileModel = ProfileModel(
                    nickName = binding.mypageReviseName.text.toString(),
                    profileImageUri = homeViewModel.profile.value?.profileImageUri,
                    address = binding.town.text.toString(),
                    introduction = binding.mypageReviseIntroduction.text.toString()
                )
                homeViewModel.updateProfile(profileModel)
                findNavController().navigate(R.id.mypage)
            }
        }
    }

    private fun cance() {
        binding.Cance.setOnClickListener {
            findNavController().navigate(R.id.mypage)
        }
    }

    //리사이클러뷰에 아이템을 뛰어우고 추가 혹은 삭제 될때마다 리사이클러뷰 갱신,
    fun dog() {
        val recyclerView = binding.dogCard
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        //리사이클러뷰 가로 스크롤
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        val adapter = DogAdapter(homeViewModel.petInfoList) { position ->
            showDeleteDialog(requireContext(), position)
        }
        recyclerView.adapter = adapter
    }

    //삭제 다이얼로그
    private fun showDeleteDialog(context: Context, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("삭제 확인")
        builder.setMessage("이 항목을 삭제하시겠습니까?")
        builder.setPositiveButton("확인") { _: DialogInterface, _: Int ->
            val adapter = binding.dogCard.adapter as DogAdapter
//            adapter.removeItem(position)
        }
        builder.setNegativeButton("취소") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        //다이얼로그 취소,확인 텍스트 색상변경
        val alertDialog = builder.create()
        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            positiveButton.setTextColor(context.resources.getColor(R.color.black))
            negativeButton.setTextColor(context.resources.getColor(R.color.red))
        }
        alertDialog.show()
    }

    private fun openGallery() { // 갤러리를 여는 함수.
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES // READ_EXTERNAL_STORAGE 권한이 있는지 확인. 앱이 사용자의 저장소에서 파일을 읽을수 있도록 허용하는 권한. android.Manifest를 import 해와야한다.
            ) != PackageManager.PERMISSION_GRANTED // PackageManager.PERMISSION_GRANTED는 권한 승인상태. 승인상태가 아니라면, 아래의 실행문을 실행하게 된다.
        ) {
            Log.d("FootprintApp", "openGallery1")
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_PERMISSION)
            // requestPermissions 메서드를 이용해 사용자에게 해당 권한을 요청함.
        } else { // 권한이 없는상태가 아니라면(= 권한이 있는 상태라면)
            Log.d("FootprintApp", "openGallery2")
            val intent =
                Intent(Intent.ACTION_PICK) // 이미지를 선택할 수 있는 액션을 가진 인텐트 객체를 생성, 선택을 하면 intent에 데이터가 담김
            intent.type = "image/*" // 갤러리에서 이미지 파일만 표시하도록 지정
            startActivityForResult(intent, REQUEST_GALLERY)
            // REQUEST_GALLERY = 1이면, 갤러리 액티비티를 시작하면서 이 액티비티에서 결과를 반환받을 것임을 알린다.
        } // 이 함수의 결과로 데이터를 선택하면 intent에 이미지 데이터(자료형은 Uri)가 담김
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("FootprintApp", "onRequestPermissionsResult1")
                    openGallery()
                } else {
                    // 권한 거부 처리
                    Toast.makeText(requireContext(), "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                    Log.d("FootprintApp", "onRequestPermissionsResult2")

                }
            }
        }
    }

    // 이미지 선택 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            imageUri = data?.data
            binding.profileImage.setImageURI(imageUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
