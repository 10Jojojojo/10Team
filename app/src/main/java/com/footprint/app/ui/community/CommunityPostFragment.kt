package com.footprint.app.ui.community


import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.footprint.app.FirebaseDatabaseManager.addCommentToFirebase
import com.footprint.app.R
import com.footprint.app.api.model.CommentModel
import com.footprint.app.api.model.PostModel
import com.footprint.app.databinding.FragmentCommunityPostBinding
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class CommunityPostFragment : Fragment(R.layout.fragment_community_post) {
    private var _binding: FragmentCommunityPostBinding? = null

    // 다른 Fragment 에서도 homeViewModel instance 를 참조 하기 위해 수정
    private val communityViewModel by activityViewModels<CommunityViewModel>()
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gson = Gson()
        val postJson = requireArguments().getString("post")
        if (postJson != null) {
            val post = gson.fromJson(postJson, PostModel::class.java)
            _binding = FragmentCommunityPostBinding.bind(view)
            binding.apply {
                rvComment.layoutManager = LinearLayoutManager(context)
                val communityAdapter = CommunityCommentAdapter(requireContext(), post.commentsList)
                rvComment.adapter = communityAdapter
                tvComment.apply {
                    text = post.commentsCount.toString()
                    setOnClickListener {
                        binding.rvComment.isVisible = !binding.rvComment.isVisible
                    }
                }
                tvContent.text = post.content
                tvTitle.text = post.title
                tvLike.text = post.likesCount.toString()
                tvNickname.text = post.authorNickname
                tvPostdate.text = post.postDate
                Glide.with(requireContext())
                    .load(R.drawable.dummy_petimage)
                    .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                    .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                    .into(ivPet)
                btAddComment.setOnClickListener {
                    // 새 댓글 객체 생성
                    val newComment =  CommentModel(
                            commentId = UUID.randomUUID().toString(),         // 댓글의 고유 식별자
                            authorId = "",          // 작성자의 ID
                            authorNickname = "",    // 작성자의 닉네임
                            authorProfileImageUrl = "", // 작성자의 프로필 이미지 URL (null 가능)
                            postDate = SimpleDateFormat(
                                "hh:mm(MM/dd)",
                                Locale.KOREA
                            ).format(Date()), // 댓글 작성 일자
                            content  =  etAddComment.text.toString(),           // 댓글 내용
                            imageUrl  = "",         // 댓글에 첨부된 이미지의 URL (null 가능)
                            likesCount = 0       // 좋아요 수 (기본값 0)
                        )

                    // 댓글 리스트에 추가
                    post.commentsList.add(newComment)
                    // 댓글 수 업데이트
                    post.commentsCount = post.commentsList.size
                    // 댓글 수 UI 업데이트
                    tvComment.text = post.commentsCount.toString()
                    // 어댑터에 데이터가 변경됨을 알림

                    //파이어베이스에 업데이트

                    addCommentToFirebase(post.key, newComment,requireContext())
                    communityAdapter.notifyDataSetChanged()
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}