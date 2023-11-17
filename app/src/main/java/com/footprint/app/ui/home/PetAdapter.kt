package com.footprint.app.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.footprint.app.R
import com.footprint.app.api.model.PetInfoWalkModel
import com.footprint.app.databinding.ItemImageWalkBinding
import com.footprint.app.util.ItemClick

class PetAdapter(private val context: Context, private val items: MutableList<PetInfoWalkModel>) : RecyclerView.Adapter<PetAdapter.ImageViewHolder>() {

    var itemClick: ItemClick? = null
    inner class ImageViewHolder(private val binding: ItemImageWalkBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            Glide.with(context)
                .load(items[adapterPosition].petImageUrl) // WalkModel에 저장된 이미지 URL
                .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                .into(binding.ivPet) // 해당 이미지를 표시할 ImageView
    }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageWalkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind()
    }
}

