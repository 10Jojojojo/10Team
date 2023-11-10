package com.footprint.app.ui.community

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.footprint.app.Constants.CREATE
import com.footprint.app.FirebaseDatabaseManager.readPostdata
import com.footprint.app.FirebaseDatabaseManager.saveCommentData
import com.footprint.app.FirebaseDatabaseManager.saveLikedata
import com.footprint.app.FirebaseDatabaseManager.savePostdata
import com.footprint.app.api.model.CommentModel
import com.footprint.app.api.model.LikeModel
import com.footprint.app.api.model.PostModel
import com.footprint.app.api.model.TagModel
import com.google.firebase.auth.FirebaseAuth

class CommunityViewModel : ViewModel() {

    private var _postList = MutableLiveData<MutableList<PostModel>>().apply {
        readPostdata(null) { it, stamp ->
            value = it
            Log.d("aaaaaa1","현재 Postdata는 ${value}")
            lastPostTimestamp = stamp
        }
    }
    val postList: LiveData<MutableList<PostModel>> = _postList
    private var _likeList =
        MutableLiveData<MutableList<LikeModel>>().apply { value = mutableListOf() }
    val likeList: LiveData<MutableList<LikeModel>> = _likeList
    private var _commentList =
        MutableLiveData<MutableList<CommentModel>>().apply { value = mutableListOf() }

    val commentList: LiveData<MutableList<CommentModel>> = _commentList

    private var lastPostTimestamp: Long? = null
    fun getLastPostTimestamp(): Long? {
        return lastPostTimestamp
    }
    private var isCommunityObserve = false
    fun setCommunityObserve(state:Boolean) {
        isCommunityObserve = state
    }

    fun getCommunityObserve(): Boolean {
        return isCommunityObserve
    }
    fun updateLastPostTimestamp(currentPostTimestamp: Long) {
        lastPostTimestamp?.let { it + currentPostTimestamp } ?: currentPostTimestamp
    }


//    fun updateProfiles(profile: ProfileModel) {
//        val currentList = _profileList.value ?: mutableListOf()
//
//        currentList.add(profile)
//
//        _profileList.value = currentList
//    }

    fun updatePost(post: PostModel) {
        val currentList = _postList.value ?: mutableListOf()
        _postList.value = currentList
        savePostdata(post){
            currentList.add(it)
            _postList.value = currentList
        }
    }

    fun updateComment(postKey:String,comment: CommentModel) {
        val currentList = _commentList.value ?: mutableListOf()

        currentList.add(comment)

        _commentList.value = currentList

        Log.d("aaaaaa1", FirebaseAuth.getInstance().currentUser?.uid ?: "")
        saveCommentData(postKey,FirebaseAuth.getInstance().currentUser?.uid ?: "", comment = comment,crud = CREATE){}
    }

    fun updateLike(postKey:String,like: LikeModel) {
        val currentList = _likeList.value ?: mutableListOf()

        currentList.add(like)

        _likeList.value = currentList
        saveLikedata(postKey,FirebaseAuth.getInstance().currentUser?.uid!!){}
    }



    fun loadPost(postList: MutableList<PostModel>) {
        _postList.value = postList
    }

//    fun loadComment(
//        commentList: MutableList<CommentModel>,
//        onCompleted: (MutableList<CommentModel>) -> Unit
//    ) {
//        _commentList.value = commentList
//    }
//
//    fun loadLike(likeList: MutableList<LikeModel>) {
//        _likeList.value = likeList
//    }


    var images = mutableListOf<Uri>()
    val dummyTag = mutableListOf(
        TagModel(tag = "Hot"),
        TagModel(tag = "전체 게시글"),
        TagModel(tag = "훈련 꿀팁"),
        TagModel(tag = "정보 교류")
    )

}