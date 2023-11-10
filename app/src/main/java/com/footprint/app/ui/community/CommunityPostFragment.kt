package com.footprint.app.ui.community


import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.footprint.app.R
import com.footprint.app.api.model.CommentModel
import com.footprint.app.databinding.FragmentCommunityPostBinding


class CommunityPostFragment : Fragment(R.layout.fragment_community_post) {
    private var _binding: FragmentCommunityPostBinding? = null

    // 다른 Fragment 에서도 communityViewModel instance 를 참조 하기 위해 수정
    private val communityViewModel by activityViewModels<CommunityViewModel>()
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val position = requireArguments().getInt("position")
        _binding = FragmentCommunityPostBinding.bind(view)
        binding.apply {
            rvComment.layoutManager = LinearLayoutManager(context)
            val communityAdapter = CommunityCommentAdapter(
                requireContext(),
                communityViewModel.postList.value?.get(position)?.comments!!
            )
            rvComment.adapter = communityAdapter
            tvComment.apply {
                text = (
                        communityViewModel.postList.value?.get(position)?.comments?.size
                            ?: 0).toString()
                setOnClickListener {
                    binding.rvComment.isVisible = !binding.rvComment.isVisible
                }
            }
            tvContent.text = communityViewModel.postList.value?.get(position)?.content
            tvTitle.text = communityViewModel.postList.value?.get(position)?.title
            tvLike.text =
                communityViewModel.postList.value?.get(position)?.likeCount.toString()// post.likesCount.toString()
            tvNickname.text =
                communityViewModel.postList.value?.get(position)?.nickname// post.authorNickname
            tvPostdate.text =
                communityViewModel.postList.value?.get(position)?.timestamp.toString()// post.postDate
            Glide.with(requireContext())
                .load(R.drawable.dummy_petimage)
                .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                .into(ivPet)
            btAddComment.setOnClickListener {
                // 새 댓글 객체 생성
                val newComment = CommentModel(
                    content = etAddComment.text.toString()
                )

                // 댓글 리스트에 추가
                communityViewModel.updateComment(
                    communityViewModel.postList.value?.get(position)?.postKey!!,
                    newComment
                )
                // 댓글 수 업데이트
                communityViewModel.postList.value?.get(position)?.commentCount
                // 댓글 수 UI 업데이트
                tvComment.text =
                    communityViewModel.postList.value?.get(position)?.commentCount.toString()
                // 어댑터에 데이터가 변경됨을 알림
                communityAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}