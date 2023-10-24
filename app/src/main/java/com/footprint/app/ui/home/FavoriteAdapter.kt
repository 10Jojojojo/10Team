package com.footprint.app.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.footprint.app.api.model.WalkModel
import com.footprint.app.databinding.WalkitemBinding
import com.footprint.app.util.ItemClick
import java.io.File

class FavoriteAdapter(private val context: Context, private val walkitems: MutableList<WalkModel>) : RecyclerView.Adapter<FavoriteAdapter.CommentViewHolder>() {

    var itemClick: ItemClick? = null
    inner class CommentViewHolder(private val binding: WalkitemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(walk: WalkModel) {
            val filePath = File(context.getExternalFilesDir(null), "map_snapshot[${walk.dateid}].png").absolutePath
            Glide.with(context).load(filePath).into(binding.ivMapImage)
            binding.tvDistancetext.text = walk.distance
            binding.tvIdtext.text = walk.name
            binding.tvTimetext.text = walk.walktime
            binding.root.setOnClickListener{
                itemClick?.onClick(it, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = WalkitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int = walkitems.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(walkitems[position])
    }

}