package com.footprint.app.ui.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.footprint.app.FirebaseDatabaseManager.readMyPostdata
import com.footprint.app.api.model.PostModel

class MyPageViewModel : ViewModel() {

    private var _myPostList = MutableLiveData<MutableList<PostModel>>().apply {
        readMyPostdata() { value = it
        }
    }
    val myPostList: LiveData<MutableList<PostModel>> = _myPostList
}