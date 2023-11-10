package com.footprint.app.ui.community

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.footprint.app.R
import com.footprint.app.api.model.CommentModel
import com.footprint.app.api.model.PostModel
import com.footprint.app.databinding.ItemCommentBinding
import com.footprint.app.util.ItemClick

class CommunityCommentAdapter(private val context: Context, private val items: MutableList<CommentModel>) :
    RecyclerView.Adapter<CommunityCommentAdapter.CommentHolder>() {

    var itemClick: ItemClick? = null


    // 생성자 의 parameter 에 val 을 사용 하여 멤버 변수로 선언
    inner class CommentHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(commentModel: CommentModel) {
            binding.apply {
                Glide.with(context)
                    .load(R.drawable.dummy_petimage)
                    .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                    .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                    .into(ivAuthorImage)
                tvNickname.text = "임시 텍스트(닉네임)"// commentModel.authorNickname
                tvPostdate.text = "임시 텍스트(게시일자)" // commentModel.postDate
                tvComment.text = commentModel.content
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        return CommentHolder(
            ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    // is 키워드 를 이용 하여 타입 검사를 한 후 타입에 따라 다른 홀더의 bind 함수를 호출
    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        holder.bind(items[position])
    }

    // 나중에, 태그에 따라 다르게 수정 해야함
    override fun getItemCount(): Int {
        return items.size
    }


}