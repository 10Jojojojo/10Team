package com.footprint.app.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.footprint.app.R
import com.footprint.app.api.model.PetInfoModel
import com.footprint.app.util.ItemClick

class DogAdapter(
    private val petInfoList: LiveData<MutableList<PetInfoModel>>,
    private val onItemLongClick: (Int) -> Unit
) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {
    var itemClick: ItemClick? = null

    class DogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.dogPlusCardName)
        val age: TextView = view.findViewById(R.id.dogPlusCardAge)
        val sex: TextView = view.findViewById(R.id.dogPlusCardSex)

        val layout: ConstraintLayout = itemView.findViewById(R.id.cl_container)
        val petImage: ImageView = view.findViewById(R.id.iv_petimage)
        val active: ImageView = view.findViewById(R.id.iv_petselect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.mydogpluscard, parent, false)
        return DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = petInfoList.value?.get(position)
        if (dog != null) {
            holder.name.text = dog.petName
            holder.age.text = dog.petAge
            holder.sex.text = dog.petSex
            if (petInfoList.value?.get(position)?.activePet == true) {
                holder.active.visibility = View.VISIBLE
            } else {
                holder.active.visibility = View.GONE
            }
            Glide.with(holder.itemView.context)
                .load(dog.petImageUrl) // 이미지 URL
                .into(holder.petImage) // ConstraintLayout에 이미지 설정
            holder.itemView.setOnClickListener {
                itemClick?.onClick(it, position)
            }
            true
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(position)
            true
        }

    }


    override fun getItemCount() = petInfoList.value?.size ?: 0

    // 삭제
//    fun removeItem(position: Int) {
//        val currentList = petInfoList.value ?: mutableListOf()
//        currentList.removeAt(position)
//        petInfoList.value = currentList
//        notifyItemRemoved(position)
//    }


}
