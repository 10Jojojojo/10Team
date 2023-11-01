package com.footprint.app.ui.community

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.footprint.app.Constants.TYPE_POST
import com.footprint.app.Constants.TYPE_TAG
import com.footprint.app.R
import com.footprint.app.api.model.PostModel
import com.footprint.app.api.model.TagModel
import com.footprint.app.databinding.ItemPostBinding
import com.footprint.app.databinding.ItemTagBinding
import com.footprint.app.util.ItemClick

class CommunityAdapter(private val context: Context, private val items: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //Any 는 모든 타입을 받을 수 있다.

    var itemClick: ItemClick? = null


    // 생성자 의 parameter 에 val 을 사용 하여 멤버 변수로 선언
    inner class PostHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(postModel: PostModel) {
            Glide.with(context)
                .load(R.drawable.dummy_petimage)
                .into(binding.ivPet)
            Glide.with(context)
                .load(R.drawable.dummy_walkitem_map)
                .into(binding.ivThumbnail)
            binding.tvNickname.text = postModel.nickname
            binding.tvPostdate.text = postModel.postDate
            binding.tvTitle.text = postModel.title
            binding.tvContent.text = postModel.content
            binding.tvLike.text = postModel.likesCount.toString()
            binding.tvComment.text = postModel.commentsCount.toString()
        }
    }

    // 생성자 의 parameter 에 val 을 사용 하여 멤버 변수로 선언
    inner class TagHolder(private val binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tagModel: TagModel) {
            binding.btTag.text = tagModel.tag
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_POST -> PostHolder(
                ItemPostBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            )

            TYPE_TAG -> TagHolder(
                ItemTagBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            )

            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    // is 키워드 를 이용 하여 타입 검사를 한 후 타입에 따라 다른 홀더의 bind 함수를 호출
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (items[position]) {
            is PostModel -> (holder as PostHolder).bind(items[position] as PostModel)
            is TagModel -> (holder as TagHolder).bind(items[position] as TagModel)
        }
    }
    // 나중에, 태그에 따라 다르게 수정 해야함
    override fun getItemCount(): Int {
        return items.size
    }

    // 2개의 Item 을 RecyclerView 로 표현 하기 위해 getItemViewType 으로 ViewType 을 나누기
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is PostModel -> TYPE_POST
            is TagModel -> TYPE_TAG
            else -> throw IllegalArgumentException("Unknown type at $position")
        }
    }
}