package com.footprint.app.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.footprint.app.api.model.WalkModel
import com.footprint.app.databinding.ItemWalkBinding
import com.footprint.app.util.ItemClick
import java.io.File

class FavoriteAdapter(private val context: Context, private val items: MutableList<WalkModel>) : RecyclerView.Adapter<FavoriteAdapter.WalkViewHolder>() {

    var itemClick: ItemClick? = null
    inner class WalkViewHolder(private val binding: ItemWalkBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(walk: WalkModel) {
            val filePath = File(context.getExternalFilesDir(null), "map_snapshot[${walk.dateid}].png").absolutePath
            Glide.with(context).load(filePath).into(binding.ivMapImage)
            binding.tvDistancetext.text = walk.distance
            binding.tvIdtext.text = walk.name
            binding.tvTimetext.text = walk.walktime
            binding.tvWalkdatevalue.text = walk.date
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
    }

}