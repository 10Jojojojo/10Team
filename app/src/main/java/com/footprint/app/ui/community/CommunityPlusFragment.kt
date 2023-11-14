package com.footprint.app.ui.community

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.footprint.app.Constants
import com.footprint.app.Constants.CREATE
import com.footprint.app.Constants.UPDATE
import com.footprint.app.FirebaseDatabaseManager
import com.footprint.app.FirebaseDatabaseManager.savePostdata
import com.footprint.app.FirebaseDatabaseManager.uploadImage
import com.footprint.app.FirebaseDatabaseManager.uploadImages
import com.footprint.app.R
import com.footprint.app.api.model.CommentModel
import com.footprint.app.api.model.PostModel
import com.footprint.app.databinding.FragmentCommunityPlusBinding
import com.footprint.app.ui.home.HomeViewModel
import com.footprint.app.ui.mypage.MyPageViewModel
import com.footprint.app.util.ItemClick
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CommunityPlusFragment : Fragment(R.layout.fragment_community_plus) {

    private var _binding: FragmentCommunityPlusBinding? = null

    // 다른 Fragment 에서도 homeViewModel instance 를 참조 하기 위해 수정
    private val communityViewModel by activityViewModels<CommunityViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val myPageViewModel by activityViewModels<MyPageViewModel>()
    private val binding get() = _binding!!

    // CommunityAdapter 의 instance 를 클래스 레벨 변수로 저장
    private lateinit var communityAdapter: CommunityAdapter
    private var getposition = 0
    private lateinit var images: MutableList<*>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCommunityPlusBinding.bind(view)
        getposition = requireArguments().getInt("position")
        initView()
    }

    private fun goCommunityPage() {
        binding.btComplete.setOnClickListener {
            if (communityViewModel.postState) {
                requireContext().uploadImages(communityViewModel.images) {
                    communityViewModel.updatePost(
                        PostModel(
                            uid = FirebaseAuth.getInstance().currentUser?.uid,
                            nickname = homeViewModel.profile.value?.nickName,
                            profileImageUri = homeViewModel.profile.value?.profileImageUri,
                            timestamp = System.currentTimeMillis(),
                            title = binding.etTitle.text.toString(), // 글 제목
                            content = binding.etContent.text.toString(), // 글 내용
                            postImageUrls = it, // 게시글의 사진 URL 리스트
                        ), CREATE
                    ) {communityViewModel.images.clear()
                        findNavController().popBackStack() // 현재 프래그먼트 백스택에서 프래그먼트를 없앤다.
                    }
//            findNavController().navigate(R.id.community)
                }
            } else {
                Log.d("aaaaaa12311", "${communityViewModel.images}")
                requireContext().uploadImages(communityViewModel.images) {
                    Log.d("aaaaaa123112", "${it}")
                    communityViewModel.updatePost(
                        PostModel(
                            postKey = communityViewModel.postList.value?.get(getposition)?.postKey,
                            timestamp = communityViewModel.postList.value?.get(getposition)?.timestamp
                                ?: 0L,
                            title = binding.etTitle.text.toString(), // 글 제목
                            content = binding.etContent.text.toString(), // 글 내용
                            postImageUrls = it, // 게시글의 사진 URL 리스트
                            uid = communityViewModel.postList.value?.get(getposition)?.uid,
                            nickname = communityViewModel.postList.value?.get(getposition)?.nickname,
                            profileImageUri = communityViewModel.postList.value?.get(getposition)?.profileImageUri,
                            commentCount = communityViewModel.postList.value?.get(getposition)?.commentCount
                                ?: 0L,
                            likeCount = communityViewModel.postList.value?.get(getposition)?.likeCount
                                ?: 0L,
                            comments = communityViewModel.postList.value?.get(getposition)?.comments
                                ?: mutableListOf(),
                            likes = communityViewModel.postList.value?.get(getposition)?.likes
                                ?: emptyMap()
                        ), UPDATE
                    ) {communityViewModel.images.clear()
                        findNavController().popBackStack() // 현재 프래그먼트 백스택에서 프래그먼트를 없앤다.
                    }
//            findNavController().navigate(R.id.community)

                }
            }
        }

    }

    private fun initView() {
//        spinnerView()
        goCommunityPage()
        recyclerView()
        binding.cvUploadImage.setOnClickListener {
            openGallery()
        }
        if (getposition != -1) {
            binding.etContent.setText(communityViewModel.postList.value?.get(getposition)?.content)
            binding.etTitle.setText(communityViewModel.postList.value?.get(getposition)?.title)
        }

    }

    private fun recyclerView() {
        communityAdapter =
            CommunityAdapter(requireContext(), communityViewModel.images).apply {
                itemClick = object : ItemClick {
                    override fun onClick(view: View, position: Int) {
                        images.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, images.size - position)
                        binding.indicatorVp2PostImage.setViewPager(binding.vp2PostImage)
                    }
                }
            }
//        binding.rvContent.layoutManager = LinearLayoutManager(requireContext())
//        binding.rvContent.adapter = communityAdapter
        binding.vp2PostImage.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 가로 스와이프 설정
        binding.vp2PostImage.adapter = communityAdapter
        // 페이지 인디케이터 설정 (옵션)
        // 예를 들어, TabLayoutMediator를 사용하여 TabLayout을 ViewPager2와 연결할 수 있습니다.
        // 이 부분은 구현에 따라 달라질 수 있습니다.
//                TabLayoutMediator(holder.binding.tabLayout, holder.binding.viewPager2) { tab, position ->
//                    // 여기에 페이지 인디케이터 설정
//                }.attach()
        // Indicator에 viewPager 설정
//        binding.indicatorVp2PostImage.setViewPager(binding.vp2PostImage)
    }

    //    private fun spinnerView() {
//        // 스피너 에 들어갈 데이터 (첫 번째 옵션 으로 안내 메시지 포함)
//        val spinnerItems = arrayListOf("카테고리를 선택해주세요.")
//        spinnerItems.addAll(communityViewModel.dummyTag.map { it.tag })
//
//        // 어댑터 설정
//        val spinnerAdapter = object : ArrayAdapter<String>(
//            requireContext(),
//            android.R.layout.simple_spinner_item,
//            spinnerItems
//        ) {
//            override fun isEnabled(position: Int): Boolean {
//                // 첫 번째 아이템 은 선택 불가능 하게 설정
//                return position != 0
//            }
//
//            override fun getDropDownView(
//                position: Int,
//                convertView: View?,
//                parent: ViewGroup
//            ): View {
//                val view: TextView =
//                    super.getDropDownView(position, convertView, parent) as TextView
//                // 첫 번째 아이템 은 회색 으로 표시 하여 선택 불가능 하게 보이게 설정
//                if (position == 0) {
//                    view.setTextColor(Color.GRAY)
//                } else {
//                    view.setTextColor(Color.BLACK)
//                }
//                return view
//            }
//        }
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//
//        // 스피너 에 어댑터 적용
//        binding.spinner.adapter = spinnerAdapter
//
//        // 스피너 선택 이벤트 리스너 설정
//        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View?, // NullPointerException 예외 때문에 view를 nullable 타입으로 변경
//                position: Int,
//                id: Long
//            ) {
//                // 첫 번째 아이템은 처리하지 않음
//                if (position > 0) {
//                    // view가 null이 아닐 때만 처리
//                    view?.let {
//                        // 선택된 항목 처리
//                        val item = parent.getItemAtPosition(position).toString()
//                    }
//                }
//            }
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // 아무것 도 선택 되지 않았을 때 기본 값으로 설정
//                binding.spinner.setSelection(0)
//            }
//        }
//        // 앱 시작시 초기 선택 설정
//        binding.spinner.setSelection(0)
//    }
    private fun openGallery() { // 갤러리를 여는 함수.

        // API 레벨에 따른 권한과 요청 코드 설정
        val permission: String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33부터는 READ_MEDIA_IMAGES 사용
                Manifest.permission.READ_MEDIA_IMAGES
            } else { // 그 이하 버전에서는 READ_EXTERNAL_STORAGE 사용
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission // READ_EXTERNAL_STORAGE 권한이 있는지 확인. 앱이 사용자의 저장소에서 파일을 읽을수 있도록 허용하는 권한. android.Manifest를 import 해와야한다.
            ) != PackageManager.PERMISSION_GRANTED // PackageManager.PERMISSION_GRANTED는 권한 승인상태. 승인상태가 아니라면, 아래의 실행문을 실행하게 된다.
        ) {
            Log.d("FootprintApp", "openGallery1")
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(permission),
                Constants.REQUEST_PERMISSION
            )
            // requestPermissions 메서드를 이용해 사용자에게 해당 권한을 요청함.
        } else { // 권한이 없는상태가 아니라면(= 권한이 있는 상태라면)
            Log.d("FootprintApp", "openGallery2")
            val intent =
                Intent(Intent.ACTION_PICK) // 이미지를 선택할 수 있는 액션을 가진 인텐트 객체를 생성, 선택을 하면 intent에 데이터가 담김
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
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_GALLERY && resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                communityViewModel.images.add(imageUri)
            }
            communityAdapter.notifyItemInserted(communityViewModel.images.size - 1)
            binding.indicatorVp2PostImage.setViewPager(binding.vp2PostImage)
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