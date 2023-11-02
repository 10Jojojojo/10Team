package com.footprint.app

import com.footprint.app.api.model.FlagModel
import com.footprint.app.api.model.PostModel
import com.footprint.app.api.model.ProfileModel
import com.footprint.app.api.model.WalkModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

object FirebaseDatabaseManager {
    private val database = Firebase.database.reference
    private val readPostState:String = "불러옴"

    // ProfileModel 저장
    fun saveProfile(profile: ProfileModel) {
        database.child("profiles").child(profile.nickName).setValue(profile)
    }

    // FlagModel 리스트 저장
    fun saveFlagList(flagList: List<FlagModel>) {
        database.child("flags").setValue(flagList)
    }

    // WalkModel 리스트 저장
    fun saveWalkList(walkList: List<WalkModel>) {
        database.child("walks").setValue(walkList)
    }

    // PostModel 리스트 저장
    fun savePostList(postList: List<PostModel>) {
        database.child("posts").setValue(postList)
    }

    // FlagModel 저장
    fun saveFlagList(flag: FlagModel) {
        database.child("flags").setValue(flag)
    }

    // WalkModel 저장
    fun saveWalkList(walk: WalkModel) {
        database.child("walks").setValue(walk)
    }

    // PostModel 저장
    fun savePostList(post: PostModel) {
//        database.child("posts").setValue(post)
        val key = database.child("posts").push().key  // 고유 키 생성
        if (key != null) {
            database.child("posts").child(key).setValue(post)
        }
    }
    // PostModel 객체의 리스트를 한 번만 읽기
//    fun readPostList(localPostList: MutableList<PostModel>) {
//        database.child("posts").addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val postsList = mutableListOf<PostModel>()
//                for (postSnapshot in dataSnapshot.children) {
//                    val post = postSnapshot.getValue(PostModel::class.java)
//                    post?.let {
//                        postsList.add(it)
//                    }
//                }
//                localPostList.addAll(postsList)
//                // 'postsList'를 사용하여 필요한 작업을 수행합니다.
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // 에러를 처리합니다.
//            }
//        })
//    }
    fun readPostList(localPostList: MutableList<PostModel>, onCompleted: () -> Unit) {
        database.child("posts").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val postsList = mutableListOf<PostModel>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(PostModel::class.java)
                    post?.let {
                        postsList.add(it)
                    }
                }
                localPostList.clear()
                localPostList.addAll(postsList)
                onCompleted() // 데이터 로드가 완료된 후 콜백 호출
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 에러를 처리합니다.
            }
        })
    }
//    // 특정 경로의 데이터를 한 번만 읽기
//    fun readOnceFromDatabase() {
//        database.child("some_child").get().addOnSuccessListener { dataSnapshot ->
//            if (dataSnapshot.exists()) {
//                // dataSnapshot의 데이터를 여기서 처리
//                val data = dataSnapshot.getValue(PostModel::class.java)
//                // 'data'는 이제 필요한 데이터 타입의 객체입니다.
//            } else {
//                // handle the case where the data does not exist
//            }
//        }.addOnFailureListener {
//            // handle the error
//        }
//    }

}