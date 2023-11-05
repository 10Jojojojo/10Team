package com.footprint.app.ui.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.footprint.app.R
import com.footprint.app.api.model.WalkModel
import com.footprint.app.databinding.ItemWalkBinding
import com.footprint.app.util.ItemClick
import java.io.File
import javax.sql.DataSource

class FavoriteAdapter(private val context: Context, private val items: MutableList<WalkModel>) : RecyclerView.Adapter<FavoriteAdapter.WalkViewHolder>() {

    var itemClick: ItemClick? = null
    inner class WalkViewHolder(private val binding: ItemWalkBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(walk: WalkModel) {
//            val filePath = File(context.getExternalFilesDir(null), "map_snapshot[${walk.dateid}].png").absolutePath
//            Glide.with(context).load(filePath).into(binding.ivMapImage)
            Glide.with(context)
                .load(items[adapterPosition].snapshotPath) // WalkModel에 저장된 이미지 URL
                .placeholder(R.drawable.gif_loading) // 로딩 중에 보여줄 이미지
                .error(R.drawable.ic_error) // 로딩 실패 시 보여줄 이미지
                .into(binding.ivMapImage) // 해당 이미지를 표시할 ImageView


//            Glide.with(context).asGif().load(R.drawable.gif_loading).into(binding.ivPlaceholder)
//            Glide.with(context).load(items[adapterPosition].snapshotPath).into(binding.ivMapImage)
            // 실제 이미지 로딩
//            Glide.with(context)
//                .load(items[adapterPosition].snapshotPath)
//                .listener(object : RequestListener<Drawable> {
//                    override fun onLoadFailed(
//                        e: GlideException?,
//                        model: Any?,
//                        target: Target<Drawable>,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        // 로딩 실패 시, GIF를 계속 표시하거나 에러 이미지로 전환할 수 있음
//                        Glide.with(context).load(R.drawable.ic_error).into(binding.ivMapImage)
//                        binding.vsMapImage.showNext() // 다음 뷰로 전환 (에러 이미지로)
//                        return false
//                    }
//
//                    override fun onResourceReady(
//                        resource: Drawable,
//                        model: Any,
//                        target: Target<Drawable>?,
//                        dataSource: com.bumptech.glide.load.DataSource,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        // 로딩 성공 시, 실제 이미지 뷰로 전환
//                        binding.vsMapImage.showNext() // 다음 뷰로 전환 (실제 이미지로)
//                        return false
//                    }
//                })
//                .into(binding.ivMapImage)
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