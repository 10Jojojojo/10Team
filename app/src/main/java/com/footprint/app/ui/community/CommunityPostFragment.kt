package com.footprint.app.ui.community


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.footprint.app.Constants.CREATE
import com.footprint.app.Constants.DELETE
import com.footprint.app.Constants.UPDATE
import com.footprint.app.FirebaseDatabaseManager.readLikedata
import com.footprint.app.FirebaseDatabaseManager.readProfiledata
import com.footprint.app.FirebaseDatabaseManager.saveCommentData
import com.footprint.app.R
import com.footprint.app.api.model.CommentModel
import com.footprint.app.api.model.PostModel
import com.footprint.app.databinding.FragmentCommunityPostBinding
import com.footprint.app.formatDateMdhm
import com.footprint.app.ui.home.HomeViewModel
import com.footprint.app.util.ItemClick
import com.google.firebase.auth.FirebaseAuth


class CommunityPostFragment : Fragment(R.layout.fragment_community_post) {
    private var _binding: FragmentCommunityPostBinding? = null

    // 다른 Fragment 에서도 communityViewModel instance 를 참조 하기 위해 수정
    private val communityViewModel by activityViewModels<CommunityViewModel>()
    private val binding get() = _binding!!
    private lateinit var communityCommentAdapter: CommunityCommentAdapter
    private lateinit var communityAdapter: CommunityAdapter
    private var getposition = 0
    private val homeViewModel by activityViewModels<HomeViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getposition = requireArguments().getInt("position")
        communityViewModel.loadComment(communityViewModel.postList.value!![getposition].comments) { comments ->
            comments.forEach { comment ->
                readProfiledata(comment.uid) { profileModel ->
                    comment.nickname = profileModel?.nickName
                    comment.profileImageUri = profileModel?.profileImageUri
                    communityViewModel.updateCommentUI() // 비동기적으로 닉네임과 프로필Url이 받아와져서, 이후 옵저브 메서드를 동작시키기 위한 메서드
                }
            }
        }
        _binding = FragmentCommunityPostBinding.bind(view)
        binding.apply {
            tvLike.text = communityViewModel.postList.value?.get(getposition)?.likeCount.toString()
            rvComment.layoutManager = LinearLayoutManager(context)
            communityCommentAdapter = CommunityCommentAdapter(
                requireContext(),
                communityViewModel.postList.value?.get(getposition)?.comments!!
            ).apply {
                itemClickUpdate = object : ItemClick {
                    override fun onClick(view: View, position: Int) {
                        if (communityViewModel.postList.value?.get(getposition)?.comments!![position].uid == FirebaseAuth.getInstance().currentUser?.uid) {
                            communityViewModel.updateComment(postKey = communityViewModel.postList.value?.get(getposition)?.postKey!!,
                                comment = communityViewModel.postList.value?.get(getposition)?.comments!![position],
                                crud = UPDATE,
                                updateComment = binding.etAddComment.text.toString()){
                            }
                        } else {
                            showToast("수정할 권한이 없습니다.")
                        }
                    }
                }
                itemClickDelete = object : ItemClick {
                    override fun onClick(view: View, position: Int) {
                        if (communityViewModel.postList.value?.get(getposition)?.comments!![position].uid == FirebaseAuth.getInstance().currentUser?.uid) {

                            communityViewModel.updateComment(
                                postKey = communityViewModel.postList.value?.get(getposition)?.postKey!!,
                                comment = communityViewModel.postList.value?.get(getposition)?.comments!![position],
                                crud = DELETE){
                                communityViewModel.postList.value?.get(getposition)?.commentCount =
                                    communityViewModel.commentList.value?.size?.toLong()!!
                                communityViewModel.updateCommentUI()
                            }
                        } else {
                            showToast("삭제할 권한이 없습니다.")
                        }
                    }
                }
            }

            rvComment.adapter = communityCommentAdapter
            tvComment.apply {
                text = (
                        communityViewModel.postList.value?.get(getposition)?.comments?.size
                            ?: 0).toString()
                setOnClickListener {
                    binding.rvComment.isVisible = !binding.rvComment.isVisible
                }
            }
            tvContent.text = communityViewModel.postList.value?.get(getposition)?.content
            tvTitle.text = communityViewModel.postList.value?.get(getposition)?.title
            tvLike.text =
                communityViewModel.postList.value?.get(getposition)?.likeCount.toString()
            tvNickname.text =
                communityViewModel.postList.value?.get(getposition)?.nickname
            tvPostdate.text =
                formatDateMdhm(communityViewModel.postList.value?.get(getposition)?.timestamp ?: 0L)
            Glide.with(requireContext())
                .load(communityViewModel.postList.value?.get(getposition)?.profileImageUri)
                .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                .into(ivPet)
            btAddComment.setOnClickListener {
                // 새 댓글 객체 생성
                val newComment = CommentModel(
                    uid = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    content = etAddComment.text.toString(),
                    nickname = homeViewModel.profile.value?.nickName
                        ?: "", // 빠르게 업로드 하기 위해 서버로부터 받아온 사용자의 정보를 임시로 할당
                    profileImageUri = homeViewModel.profile.value?.profileImageUri
                        ?: "", // 빠르게 업로드 하기 위해 서버로부터 받아온 사용자의 정보를 임시로 할당
                )

                // 댓글 리스트에 추가
                communityViewModel.updateComment(
                    communityViewModel.postList.value?.get(getposition)?.postKey!!,
                    newComment,CREATE
                ){
                    communityViewModel.postList.value?.get(getposition)?.commentCount =
                        communityViewModel.commentList.value?.size?.toLong()!!
                }
                etAddComment.setText("")
            }
            ivFavorite.setOnClickListener {
                communityViewModel.let {
                    it.updateLike(it.postList.value?.get(getposition)?.postKey!!) {
                            count -> it.postList.value!![getposition].likeCount = count
                        Log.d("aaaaaa123124","${communityViewModel.likeState.value}")
                    }
                }
            }
            tvUpdate.setOnClickListener {
                if (communityViewModel.postList.value?.get(getposition)?.uid == FirebaseAuth.getInstance().currentUser?.uid) {
                    //수정 코드 구현
                    val bundle = Bundle().apply {
                        putInt("position", getposition)
                    }
                    communityViewModel.postList.value?.get(getposition)?.postImageUrls?.let {
                        for (i in it) {
                            communityViewModel.images.add(Uri.parse(i))
                        }
                    }
                    communityViewModel.postState = false
                    findNavController().navigate(R.id.communityPlus, bundle)
                }
            }
            tvDelete.setOnClickListener {
                if (communityViewModel.postList.value?.get(getposition)?.uid == FirebaseAuth.getInstance().currentUser?.uid) {
                    //삭제 코드 구현
                    communityViewModel.updatePost(
                        communityViewModel.postList.value?.get(getposition)!!,
                        DELETE
                    ) {
                        findNavController().popBackStack()
                    }
                }
            }
        }
        communityViewModel.commentList.observe(viewLifecycleOwner) {
            // 댓글 수 UI 업데이트
            binding.tvComment.text =
                communityViewModel.commentList.value?.size.toString()
            // 어댑터에 데이터가 변경됨을 알림
            communityCommentAdapter.notifyDataSetChanged()
        }
        communityViewModel.likeState.observe(viewLifecycleOwner) {
                binding.tvLike.text =
                    communityViewModel.postList.value?.get(getposition)?.likeCount.toString()
            Log.d("aaaaaa123122",
                communityViewModel.postList.value?.get(getposition)?.likeCount.toString()
            )
        }
        recyclerView()
//        FirebaseDatabaseManager.readCommentdata(postkey!!){}
        readLikedata(communityViewModel.postList.value?.get(getposition)?.postKey!!, FirebaseAuth.getInstance().currentUser?.uid ?: "")
        {
            communityViewModel.loadLike(it)
        }

        binding.back.setOnClickListener {
            findNavController().navigate(R.id.navigation_community)
        }
    }

    private fun recyclerView() {
        communityAdapter =
            CommunityAdapter(requireContext(),
                communityViewModel.postList.value?.get(getposition)?.postImageUrls
                    ?: mutableListOf<PostModel>()
            )
        binding.vp2PostImage.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 가로 스와이프 설정
        binding.vp2PostImage.adapter = communityAdapter
        binding.indicatorVp2PostImage.setViewPager(binding.vp2PostImage)
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