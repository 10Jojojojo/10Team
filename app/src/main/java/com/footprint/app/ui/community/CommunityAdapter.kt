package com.footprint.app.ui.community

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.footprint.app.Constants.TYPE_POST
import com.footprint.app.Constants.TYPE_TAG
import com.footprint.app.Constants.TYPE_IMAGE
import com.footprint.app.R
import com.footprint.app.api.model.PostModel
import com.footprint.app.api.model.TagModel
import com.footprint.app.databinding.ItemImageBinding
import com.footprint.app.databinding.ItemPostBinding
import com.footprint.app.databinding.ItemTagBinding
import com.footprint.app.formatDateMd
import com.footprint.app.formatDateMdhm
import com.footprint.app.formatDateYmd
import com.footprint.app.util.ItemClick

class CommunityAdapter(private val context: Context, private val items: List<*>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //Any 는 모든 타입을 받을 수 있다.
    //* 는 파라미터 타입을 지정하지
    // 전혀 관련없는 뷰홀더는 여러개의 어댑터로 만들기

    var itemClick: ItemClick? = null

    var itemClickUpdate: ItemClick? = null
    var itemClickDelete: ItemClick? = null

    // 생성자 의 parameter 에 val 을 사용 하여 멤버 변수로 선언
    inner class PostHolder(val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(postModel: PostModel) {
            Glide.with(context)
                .load(postModel.profileImageUri)
                .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                .into(binding.ivProfileImage)
            binding.apply {
                tvNickname.text = postModel.nickname
                tvPostdate.text = formatDateMdhm(postModel.timestamp)
                tvTitle.text = postModel.title
                tvContent.text = postModel.content
                tvLike.text = postModel.likeCount.toString()
                tvComment.text = postModel.comments.size.toString()
                root.setOnClickListener {
                    itemClick?.onClick(it, adapterPosition)
                }
            }
        }
    }

    // 생성자 의 parameter 에 val 을 사용 하여 멤버 변수로 선언
    inner class TagHolder(private val binding: ItemTagBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tagModel: TagModel) {
            binding.btTag.text = tagModel.tag
        }
    }

    inner class ImageHolder(private val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(postImageUrl: Uri) {
            Glide.with(context)
                .load(postImageUrl)
                .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                .into(binding.ivImage)
            binding.ivImage.setOnClickListener {
                itemClick?.onClick(it, adapterPosition)
            }
        }
        fun bind(postImageUrl: String) {
            Glide.with(context)
                .load(postImageUrl)
                .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                .into(binding.ivImage)
            binding.ivImage.setOnClickListener {
                itemClick?.onClick(it, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_POST -> PostHolder(
                ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            TYPE_TAG -> TagHolder(
                ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            TYPE_IMAGE -> ImageHolder(
                ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    // is 키워드 를 이용 하여 타입 검사를 한 후 타입에 따라 다른 홀더의 bind 함수를 호출
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (items[position]) {
            is PostModel -> {
                (holder as PostHolder).bind(items[position] as PostModel)
                // RecyclerView 안에 새로운 RecyclerView 를 추가 하려면 onBindViewHolder 에서 RecyclerView Adapter 를 정의해 주어야 한다.
                // 동일한 Item 과 동일한 Adapter 를 사용 하므로, CommunityAdapter 를 그대로 사용 한다.
                // 이미지를 가로가 아닌 세로로 볼수있도록
                // 뷰페이저2로 만드는것을 고민? 좌우스와이프? 뷰페이저2도 어댑터가 있는데 얘도 리사이클러뷰 어댑터를 사용함
                // 각 포스트 항목들이 뷰페이저를 가지고있음
//                holder.binding.rvImage.layoutManager = LinearLayoutManager(context)
//                holder.binding.rvImage.adapter = CommunityAdapter(context, (items[position] as PostModel).postImageUrl)

                // ViewPager2로 변경
                val postImages = (items[position] as PostModel).postImageUrls
                holder.binding.vp2PostImage.adapter = CommunityAdapter(context, postImages)
                holder.binding.vp2PostImage.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 가로 스와이프 설정

                // 페이지 인디케이터 설정 (옵션)
                // 예를 들어, TabLayoutMediator를 사용하여 TabLayout을 ViewPager2와 연결할 수 있습니다.
                // 이 부분은 구현에 따라 달라질 수 있습니다.
//                TabLayoutMediator(holder.binding.tabLayout, holder.binding.viewPager2) { tab, position ->
//                    // 여기에 페이지 인디케이터 설정
//                }.attach()
                // Indicator에 viewPager 설정
                holder.binding.indicatorVp2PostImage.setViewPager(holder.binding.vp2PostImage)

            }

            is TagModel -> (holder as TagHolder).bind(items[position] as TagModel)
            is Uri -> (holder as ImageHolder).bind(items[position] as Uri)
            is String -> (holder as ImageHolder).bind(items[position] as String)
        }
    }

    // 나중에, 태그에 따라 다르게 수정 해야함
    override fun getItemCount(): Int {
        return items.size
    }

    // 여러 개의 Item 을 RecyclerView 로 표현 하기 위해 getItemViewType 으로 ViewType 을 나누기
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is PostModel -> TYPE_POST
            is TagModel -> TYPE_TAG
            is Uri -> TYPE_IMAGE
            is String -> TYPE_IMAGE
            else -> throw IllegalArgumentException("Unknown type at $position")
        }
    }
}