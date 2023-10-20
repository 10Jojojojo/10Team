package com.footprint.app.ui.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyPageViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is mypage Fragment"
    }
    val text: LiveData<String> = _text

    var name: String = "이름"
    var introduction : String = "자기소개"
    var town : String = "동네"
}