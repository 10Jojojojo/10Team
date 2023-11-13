package com.footprint.app.ui.community

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.footprint.app.Constants.CREATE
import com.footprint.app.Constants.DELETE
import com.footprint.app.Constants.UPDATE
import com.footprint.app.FirebaseDatabaseManager.readPostdata
import com.footprint.app.FirebaseDatabaseManager.saveCommentData
import com.footprint.app.FirebaseDatabaseManager.saveLikedata
import com.footprint.app.FirebaseDatabaseManager.savePostdata
import com.footprint.app.api.model.CommentModel
import com.footprint.app.api.model.PostModel
import com.footprint.app.api.model.TagModel
import com.google.firebase.auth.FirebaseAuth

class CommunityViewModel : ViewModel() {

    private var _postList = MutableLiveData<MutableList<PostModel>>().apply {
        readPostdata(null) { it, stamp ->
            value = it
            lastPostTimestamp = stamp
        }
    }
    val postList: LiveData<MutableList<PostModel>> = _postList
    private var _likeState =
        MutableLiveData<Boolean>().apply { value = false }
    val likeState: LiveData<Boolean> = _likeState
    private var _commentList =
        MutableLiveData<MutableList<CommentModel>>().apply { value = mutableListOf() }

    val commentList: LiveData<MutableList<CommentModel>> = _commentList

    private var lastPostTimestamp: Long? = null
    fun getLastPostTimestamp(): Long? {
        return lastPostTimestamp
    }
    private var isCommunityObserve = false

    var currentPostposition = 0
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

    fun updateComment(postKey: String, comment: CommentModel, crud: Int,updateComment:String? = null,onCompleted: () -> Unit) {
        when (crud) {
            CREATE -> {
                val currentList = _commentList.value ?: mutableListOf()

                currentList.add(comment)

                saveCommentData(
                    postKey = postKey,
                    uid = comment.uid,
                    commentKey = comment.commentKey,
                    comment = comment,
                    crud = CREATE
                ) {
                    comment.commentKey = it
                    _commentList.value = currentList
                    onCompleted()
                }
            }
            DELETE -> {
                val currentList = _commentList.value ?: mutableListOf()

                currentList.remove(comment)

                saveCommentData(
                    postKey = postKey,
                    uid = comment.uid,
                    commentKey = comment.commentKey,
                    comment = comment,
                    crud = DELETE
                ) {
                    _commentList.value = currentList
                    onCompleted()
                }
            }
            UPDATE-> {
                // 기존 리스트 복사
                // 기존 객체를 참조하는것이 아닌, 새로운 리스트를 만들어야 데이터 수정이 가능한 듯 함
                val currentList = _commentList.value?.toMutableList()

                // 특정 댓글을 찾아 수정
                val index = currentList?.indexOfFirst { it.commentKey == comment.commentKey }
                if (index != null) {
                    currentList[index].content = updateComment ?: ""
                }

                saveCommentData(
                    postKey = postKey,
                    uid = comment.uid,
                    commentKey = comment.commentKey,
                    comment = comment,
                    crud = UPDATE
                ) {
                    comment.commentKey = it
                    _commentList.value = currentList
                    onCompleted()
                }
            }
        }
    }

    fun updateLike(postKey:String,onCompleted: (Long) -> Unit) {
        // 내 좋아요 상태를 알려주는 상태변수만 정의 라이크모델 이런거까지 필요없음.

        saveLikedata(postKey,FirebaseAuth.getInstance().currentUser?.uid!!){

            onCompleted(it)
            var likeState = _likeState.value

            likeState = !likeState!!

            _likeState.value = likeState
        }
    }
    fun loadLike(likeState:Boolean) {
        _likeState.value = likeState
    }


    fun loadPost(postList: MutableList<PostModel>) {
        _postList.value = postList
    }

    fun updateCommentUI(){
        val currentComments = commentList.value ?: mutableListOf()
        _commentList.value = currentComments
    }
    fun loadComment(
        commentList: MutableList<CommentModel>,
        onCompleted: (MutableList<CommentModel>) -> Unit
    ) {
        onCompleted(commentList)
        _commentList.value = commentList
    }
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