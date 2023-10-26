package com.footprint.app.ui.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel



class MyPageViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is mypage Fragment"
    }
    val text: LiveData<String> = _text

    val dogList = MutableLiveData<MutableList<Dog>>()

    var name: String = "이름"
    var introduction : String = "자기소개"
    var town : String = "동네"

    fun addDog(dog: String, dogAge: Int, dogSex: String) {
        val currentList = dogList.value ?: mutableListOf()
        currentList.add(Dog(dog, dogAge, dogSex))
        dogList.value = currentList
    }

}
data class Dog(val dogname: String, val dogage: Int, val dogsex: String)