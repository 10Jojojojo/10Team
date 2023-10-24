package com.footprint.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.footprint.app.api.model.WalkModel
import com.footprint.app.databinding.WalkitemBinding

class FavoriteAdapter(private val comments: ArrayList<WalkModel>) : RecyclerView.Adapter<FavoriteAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: WalkitemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(walk: WalkModel) {
//            Glide.with(binding.root.context).
//            load(comment.authorProfileImageUrl).
//            into(binding.authorImage)
//
//            binding.authorName.text = comment.authorDisplayName
//            binding.commentText.text = comment.textOriginal
//            binding.commentDate.text = comment.publishedAt
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = WalkitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

}