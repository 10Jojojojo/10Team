package com.footprint.app.ui.mypage

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.footprint.app.R

class DogAdapter(private val dogList: MutableLiveData<MutableList<Dog>>) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {
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

            //롱클릭시 삭제를 묻는 다이얼로그 호출
            holder.itemView.setOnLongClickListener {
                showDeleteDialog(holder.itemView.context, position)
                true
            }
        }
    }

    override fun getItemCount() = dogList.value?.size ?: 0

    //삭제
    private fun removeItem(position: Int){
        val currentList = dogList.value ?: mutableListOf()
        currentList.removeAt(position)
        dogList.value = currentList
        notifyItemRemoved(position)
    }

    private fun showDeleteDialog(context: Context, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("삭제 확인")
        builder.setMessage("이 항목을 삭제하시겠습니까?")
        builder.setPositiveButton("확인") { _: DialogInterface, _: Int ->
            removeItem(position)
        }
        builder.setNegativeButton("취소") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }


        //다이얼로그 확인, 취소 텍스트 컬러 설정
        val alertDialog = builder.create()
        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            positiveButton.setTextColor(context.resources.getColor(R.color.black))
            negativeButton.setTextColor(context.resources.getColor(R.color.red))
        }
        alertDialog.show()
    }
}

