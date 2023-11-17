package com.footprint.app.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.footprint.app.R
import com.footprint.app.api.model.WalkModel
import com.footprint.app.databinding.ItemWalkBinding
import com.footprint.app.formatDateYmd
import com.footprint.app.formatDistance
import com.footprint.app.formatTimeMinSec
import com.footprint.app.util.ItemClick

class FavoriteAdapter(private val context: Context, private val items: MutableList<WalkModel>) : RecyclerView.Adapter<FavoriteAdapter.WalkViewHolder>() {

    var itemClick: ItemClick? = null
    inner class WalkViewHolder(val binding: ItemWalkBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(walk: WalkModel) {
            Glide.with(context)
                .load(items[adapterPosition].snapshotPath) // WalkModel에 저장된 이미지 URL
                .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                .into(binding.ivMapImage) // 해당 이미지를 표시할 ImageView

            binding.tvDistancetext.text = formatDistance(walk.distance)
//            binding.tvIdtext.text = items[adapterPosition].petList[0].petName
            binding.tvTimetext.text = formatTimeMinSec(walk.walktime)
            binding.tvWalkdatevalue.text =
                formatDateYmd(walk.starttime)
            binding.root.setOnClickListener{
                itemClick?.onClick(it, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkViewHolder {
        val binding = ItemWalkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WalkViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: WalkViewHolder, position: Int) {
        holder.bind(items[position])
        holder.binding.rvPet.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        holder.binding.rvPet.adapter =
            PetAdapter(context, items[position].petList)
    }

}