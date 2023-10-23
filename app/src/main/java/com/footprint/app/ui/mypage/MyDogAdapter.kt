package com.footprint.app.ui.mypage

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.footprint.app.R

class DogAdapter(
    private val dogList: MutableLiveData<MutableList<Dog>>,
    private val onItemLongClick: (Int) -> Unit
) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {

    class DogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.dogPlusCardName)
        val age: TextView = view.findViewById(R.id.dogPlusCardAge)
        val sex: TextView = view.findViewById(R.id.dogPlusCardSex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mydogpluscard, parent, false)
        return DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = dogList.value?.get(position)
        if (dog != null) {
            holder.name.text = dog.dogname
            holder.age.text = dog.dogage.toString()
            holder.sex.text = dog.dogsex

            holder.itemView.setOnLongClickListener {
                onItemLongClick(position)
                true
            }

        }
    }

    override fun getItemCount() = dogList.value?.size ?: 0

    // 삭제
    fun removeItem(position: Int) {
        val currentList = dogList.value ?: mutableListOf()
        currentList.removeAt(position)
        dogList.value = currentList
        notifyItemRemoved(position)
    }


}
