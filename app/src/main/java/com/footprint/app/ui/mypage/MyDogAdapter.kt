package com.footprint.app.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.footprint.app.R

class DogAdapter(private val dogList: MutableLiveData<MutableList<Dog>>) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {
    class DogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.dogName)
        val age: TextView = view.findViewById(R.id.dogAge)
        val sex: TextView = view.findViewById(R.id.dogSex)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mydogpluscard, parent, false)
        return DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = dogList.value?.get(position)
        if (dog != null) {
            holder.name.text = dog.name
            holder.age.text = dog.age.toString()
            holder.sex.text = dog.sex
        }
    }

    override fun getItemCount() = dogList.value?.size ?: 0
}
