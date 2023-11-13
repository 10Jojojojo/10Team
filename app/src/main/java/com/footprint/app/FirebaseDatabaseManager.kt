package com.footprint.app

import android.content.Context
import android.net.Uri
import android.util.Log
import com.footprint.app.Constants.CREATE
import com.footprint.app.Constants.DELETE
import com.footprint.app.Constants.UPDATE
import com.footprint.app.api.model.CommentModel
import com.footprint.app.api.model.MarkerModel
import com.footprint.app.api.model.MarkerModelDTO
import com.footprint.app.api.model.PetInfoModel
import com.footprint.app.api.model.PetInfoModelDTO
import com.footprint.app.api.model.PostModel
import com.footprint.app.api.model.PostModelDTO
import com.footprint.app.api.model.ProfileModel
import com.footprint.app.api.model.WalkModel
import com.footprint.app.api.model.WalkModelDTO
import com.footprint.app.api.model.toDTO
import com.footprint.app.api.model.toModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

object FirebaseDatabaseManager {
    private val database = Firebase.database.reference

    // 로그인 시 얻은 uid값을 사용한다. uid값을 사용할 때 값을 초기화한다.
    private val uid by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    fun savePostdata(postdata: PostModel, onCompleted: (PostModel) -> Unit) {
        val key = database.child("posts").push().key
        if (key != null) {
            database.child("posts").child(key).setValue(postdata.toDTO())
            onCompleted(postdata)
        }
    }

//    fun readPostdata(
//        lastPostTimestamp: Long?,
//        onCompleted: (MutableList<PostModel>, Long?) -> Unit
//    ) {
//        // 쿼리 시작점을 설정. 처음에는 lastPostTimestamp가 null이므로 최신 포스트부터 시작한다.
//        var query = database.child("posts").orderByChild("timestamp")
//        if (lastPostTimestamp != null) {
//            // 이미 몇 개의 포스트를 로드했다면, 마지막으로 불러온 포스트의 타임스탬프를 사용하여 그 다음 포스트부터 쿼리
//            query = query.endBefore(lastPostTimestamp.toDouble()).limitToLast(10)
//        } else {
//            query = query.limitToLast(10)
//        }
//
//        query.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val postsList = mutableListOf<PostModel>()
//                var newLastTimestamp: Long? = null
//
//                // 데이터 스냅샷을 거꾸로 순회하여 목록을 만든다.
//                val reversedData = dataSnapshot.children.reversed()
//                for (postSnapshot in reversedData) {
//                    val postDTO = postSnapshot.getValue(PostModelDTO::class.java)
//                    postDTO?.let {
//                        // 각 PostModel 객체에 대해 필요한 경우 여기서 초기화를 할 수 있다.
//                        // 예를 들어, 만약 Firebase에서 빈 배열이 아니라 null로 반환될 경우 여기서 처리한다.
//                        // 빈 데이터를 Firebase에 업로드한 경우 Firebase에서 데이터를 받아올때, 아무 값을 가지지 않기때문에 여기서 초기화를 진행한다.
//                        if (it.postImageUrls == null) {
//                            it.postImageUrls = mutableListOf()
//                        }
//                        if (it.commentCount != null) {
//                            it.commentCount = postSnapshot.child("comments").childrenCount
//                        } else {
//                            it.commentCount = 0L
//                        }
//                        if (it.likeCount != null) {
//
//                            it.likeCount = postSnapshot.child("likes").childrenCount
//                        } else {
//                            it.likeCount = 0L
//                        }
//
//                        postsList.add(it.toModel())
//                        // 새로운 마지막 타임스탬프를 업데이트
//                        newLastTimestamp = it.timestamp
//                    }
//                }
//                //콜백함수를 통해, postList를 반환하고 viewModel의 list에 더해준다. 그리고 LastTimestamp를 viewModel에 저장하여 다음에 데이터를 불러올 때 이용한다.
//                onCompleted(postsList, newLastTimestamp)
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // 에러 처리
//                databaseError.toException().printStackTrace()
//            }
//        })
//    }

    //    fun readPostdata(
//        lastPostTimestamp: Long?,
//        onCompleted: (MutableList<PostModel>, Long?) -> Unit
//    ) {
//        // 쿼리 시작점을 설정. 처음에는 lastPostTimestamp가 null이므로 최신 포스트부터 시작한다.
//        var query = database.child("posts").orderByChild("timestamp")
//        if (lastPostTimestamp != null) {
//            // 이미 몇 개의 포스트를 로드했다면, 마지막으로 불러온 포스트의 타임스탬프를 사용하여 그 다음 포스트부터 쿼리
//            query = query.endBefore(lastPostTimestamp.toDouble()).limitToLast(10)
//        } else {
//            query = query.limitToLast(10)
//        }
//
//        query.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val temporaryPostsList = mutableListOf<PostModel>() // 임시로 데이터를 저장할 리스트
//                val postsList = mutableListOf<PostModel>()
//                var newLastTimestamp: Long? = null
//                val userProfilesMap = mutableMapOf<String, ProfileModel>()
//
//                val postSnapshots = dataSnapshot.children.reversed()
//                for (postSnapshot in postSnapshots) {
//                    val postDTO = postSnapshot.getValue(PostModelDTO::class.java)
//                    postDTO?.let {
//                        // 각 PostModel 객체에 대해 필요한 경우 여기서 초기화를 할 수 있다.
//                        // 예를 들어, 만약 Firebase에서 빈 배열이 아니라 null로 반환될 경우 여기서 처리한다.
//                        // 빈 데이터를 Firebase에 업로드한 경우 Firebase에서 데이터를 받아올때, 아무 값을 가지지 않기때문에 여기서 초기화를 진행한다.
//                        if (it.postImageUrls == null) {
//                            it.postImageUrls = mutableListOf()
//                        }
//                        if (it.commentCount != null) {
//                            it.commentCount = postSnapshot.child("comments").childrenCount
//                        } else {
//                            it.commentCount = 0L
//                        }
//                        it.postKey = postSnapshot.key
//                        if (it.likeCount != null) {
//
//                            it.likeCount = postSnapshot.child("likes").childrenCount
//                        } else {
//                            it.likeCount = 0L
//                        }
//
//                        temporaryPostsList.add(it.toModel())
//                        // 새로운 마지막 타임스탬프를 업데이트
//                        newLastTimestamp = it.timestamp
//                        if (it.uid != null) {
//                            userProfilesMap[it.uid!!] = ProfileModel() // 임시 프로필 객체를 생성
//                        }
//                    }
//                }
//
//                val userProfileTasks = userProfilesMap.keys.map { uid ->
//                    database.child("profiles").child(uid).get()
//                }
//
//                Log.d("aaaaaa1","$userProfileTasks")
//                Tasks.whenAllSuccess<DataSnapshot>(userProfileTasks)
//                    .addOnSuccessListener { userProfiles ->
//                        userProfiles.forEach { snapshot ->
//                            val profile = snapshot.getValue(ProfileModel::class.java)
//                            profile?.let {
//                                userProfilesMap[snapshot.key!!] = it // 프로필 맵 업데이트
//                            }
//                        }
//                        temporaryPostsList.forEach { tempPost ->
//                            val profile = userProfilesMap[tempPost.uid]
//                            Log.d("aaaaaa1","$profile")
//                            val post = tempPost.toProfile(profile ?: ProfileModel())
//                            postsList.add(post)
//                            newLastTimestamp = tempPost.timestamp
//                        }
//                        onCompleted(postsList, newLastTimestamp)
//                    }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Handle the error
//                databaseError.toException().printStackTrace()
//            }
//        })
//    }
    fun readPostdata(
        lastPostTimestamp: Long?,
        onCompleted: (MutableList<PostModel>, Long?) -> Unit
    ) {
        var query = database.child("posts").orderByChild("timestamp")
        if (lastPostTimestamp != null) {
            query = query.endBefore(lastPostTimestamp.toDouble()).limitToLast(10)
        } else {
            query = query.limitToLast(10)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val temporaryPostsList = mutableListOf<PostModel>()
                var newLastTimestamp: Long? = null
                val userProfilesMap = mutableMapOf<String, Task<DataSnapshot>>()

                dataSnapshot.children.reversed().forEach { postSnapshot ->
                    val post = postSnapshot.getValue(PostModelDTO::class.java)
                    post?.let {
                        it.postKey = postSnapshot.key
                        it.commentCount = postSnapshot.child("comments").childrenCount
                        it.likeCount = postSnapshot.child("likes").childrenCount
                        newLastTimestamp = it.timestamp
                        temporaryPostsList.add(it.toModel())
                        Log.d("aaaaaa11","${temporaryPostsList}")
                        it.uid?.let { uid ->
                            userProfilesMap[uid] = database.child("profiles").child(uid).get()
                        }
                    }
                }
                // 모든 프로필 조회 작업이 완료되면 실행
                Tasks.whenAllSuccess<DataSnapshot>(userProfilesMap.values.toList())
                    .addOnSuccessListener { userProfiles ->
                        // 프로필 데이터를 Map 형태로 변환
                        val profiles = userProfiles.associateBy { it.key }.mapValues { entry ->
                            entry.value.getValue(ProfileModel::class.java)
                        }
                        // 각 포스트에 해당하는 프로필 데이터를 추가
                        val postsList = temporaryPostsList.map { post ->
                            post.apply {
                                val profile = profiles[post.uid]
                                nickname = profile?.nickName
                                profileImageUri = profile?.profileImageUri
                            }
                        }

                        Log.d("aaaaaa12","${postsList}")
                        // 최종적으로 변환된 포스트 리스트를 콜백을 통해 반환
                        onCompleted(postsList.toMutableList(), newLastTimestamp)
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun saveLikedata(postKey: String, uid: String, onCompleted: (Boolean) -> Unit) {
        val userLike = database.child("user_likes").child(uid).child(postKey)
        val postLike = database.child("posts").child(postKey).child("likes").child(uid)

        // 좋아요 상태 확인
        postLike.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // 좋아요가 이미 있을 경우, 삭제
                    postLike.removeValue()
                    userLike.removeValue()
                    // 콜백함수를 통해 UI 업데이트
                    onCompleted(false)
                } else {
                    // 좋아요가 없을 경우, 추가
                    val currentTime = System.currentTimeMillis()
                    postLike.setValue(currentTime)
                    userLike.setValue(currentTime)
                    // 콜백함수를 통해 UI 업데이트
                    onCompleted(true)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
    fun readLikedata(postKey: String, uid: String, onCompleted: (Boolean) -> Unit) {
        val postLike = database.child("posts").child(postKey).child("likes").child(uid)

        // 좋아요 상태 확인
        postLike.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // 좋아요가 이미 있을 경우
                    onCompleted(true)
                } else {
                    // 좋아요가 없을 경우
                    onCompleted(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 에러 처리
                onCompleted(false)
            }
        })
    }
    fun saveCommentData(
        postKey: String,
        uid: String,
        commentKey: String?,
        comment: CommentModel?,
        crud: Int,
        onCompleted: (String) -> Unit
    ) {
        comment?.let {
            it.uid = uid
        }
        val postComments = database.child("posts").child(postKey).child("comments")
        val userComments = database.child("user_comments").child(uid).child(postKey)
//        database.child("posts").child(postKey).child("commentCount").setValue()
        when (crud) {
            CREATE -> {
                if (commentKey == null && comment != null) {
                    val key = postComments.push().key
                    key?.let {
                        comment.commentKey = key // 키 주입해서 파이어베이스에 업로드
                        postComments.child(key).setValue(comment).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                userComments.child(key).setValue(System.currentTimeMillis())
                                    .addOnCompleteListener {
                                        onCompleted(key)
                                    }
                            } else {
                            }
                        }
                    }
                }
            }

            DELETE -> {
                if (commentKey != null) {
                    postComments.child(commentKey).removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            userComments.child(commentKey).removeValue()
                                .addOnCompleteListener { userTask ->
                                }
                        } else {
                        }
                    }
                }
            }

            UPDATE -> {
                if (commentKey != null && comment != null) {
                    postComments.child(commentKey).setValue(comment).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            userComments.child(commentKey).setValue(System.currentTimeMillis())
                                .addOnCompleteListener { userTask ->
                                }
                        } else {
                        }
                    }
                }
            }
        }
    }
    fun readCommentdata(postKey: String, onCompleted: (MutableList<CommentModel>) -> Unit) {
        database.child("posts").child(postKey).child("comments")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val commentList = mutableListOf<CommentModel>()

                    dataSnapshot.children.forEach { commentSnapshot ->
                        val commentKey = commentSnapshot.key
                        val comment = commentSnapshot.getValue(CommentModel::class.java)
                        comment?.let {
                            it.commentKey = commentKey // Map의 키를 CommentModel의 commentKey 필드에 할당
                            commentList.add(it)
                        }
                    }

                    onCompleted(commentList) // 데이터 로드가 완료된 후 콜백 호출
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 에러 처리
                    databaseError.toException().printStackTrace()
                    onCompleted(mutableListOf()) // 에러 발생 시 빈 리스트를 반환
                }
            })
    }


    // MarkerModel 리스트 저장
    fun saveMarkerdata(
        marker: MarkerModel,
        onCompleted: (MarkerModel?) -> Unit
    ) {
        val key = database.child("markers")
            .push().key // key값이 필요한 이유는, 같은사람이 댓글 2개달면 같은게 되버림.
        if (key != null) {
            database.child("markers").child(key).setValue(marker.toDTO())
            onCompleted(marker)
        }
    }

    fun readMarkerdata(onCompleted: (MutableList<MarkerModel>) -> Unit) {
        database.child("markers")
            .orderByChild("uid") // 마커의 UID를 기반으로 정렬
            .equalTo(uid) // 해당 UID를 가진 마커만 필터링
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val markerList = mutableListOf<MarkerModel>()
                    for (markerSnapshot in dataSnapshot.children) {
                        val markerDTO = markerSnapshot.getValue(MarkerModelDTO::class.java)?.apply {
                            markerKey = markerSnapshot.key // 마커의 키 저장
                        }
                        markerDTO?.let {
                            markerList.add(it.toModel().apply {
                                markerKey = markerDTO.markerKey // DTO로부터 모델로 키 복사
                            })
                        }
                    }
                    onCompleted(markerList) // 데이터 로드가 완료된 후 콜백 호출
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 에러 처리
                    databaseError.toException().printStackTrace()
                    onCompleted(mutableListOf()) // 에러 발생 시 빈 리스트를 반환
                }
            })
    }

    fun deleteMarkerdata(key: String, onCompleted: (Boolean) -> Unit) {
        val markerRef = database.child("markers").child(key)
        markerRef.removeValue().addOnCompleteListener { task ->
            onCompleted(task.isSuccessful)
        }
    }

    // WalkModel 리스트 저장
    fun saveWalkdata(
        walk: WalkModel,
        onCompleted: (WalkModel?) -> Unit
    ) {
        val key = database.child("markers")
            .push().key // key값이 필요한 이유는, 같은사람이 댓글 2개달면 같은게 되버림.
        if (key != null) {
            database.child("walks").child(key).setValue(walk.toDTO())
            onCompleted(walk)
        }
    }

    fun readWalkdata(onCompleted: (MutableList<WalkModel>) -> Unit) {
        database.child("walks")
            .orderByChild("uid") // 마커의 UID를 기반으로 정렬
            .equalTo(uid) // 해당 UID를 가진 마커만 필터링
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val walkList = mutableListOf<WalkModel>()
                    for (walkSnapshot in dataSnapshot.children) {
                        val walkDTO = walkSnapshot.getValue(WalkModelDTO::class.java)
                        walkDTO?.let {
                            walkList.add(it.toModel())
                        }
                    }
                    onCompleted(walkList) // 데이터 로드가 완료된 후 콜백 호출
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 에러 처리
                    databaseError.toException().printStackTrace()
                    onCompleted(mutableListOf()) // 에러 발생 시 빈 리스트를 반환
                }
            })
    }

    fun savePetinfodata(
        petInfo: PetInfoModel,
        onCompleted: (PetInfoModel?) -> Unit
    ) {
        val key = database.child("pets").push().key // key값이 필요한 이유는, 같은사람이 댓글 2개달면 같은게 되버림.
        if (key != null) {
            database.child("pets").child(uid).child(key).setValue(petInfo)
            onCompleted(petInfo)
        }
    }

    fun savePetInfoDataActive(
        timestamp: Long,
        onCompleted: () -> Unit
    ) {
        // Firebase에서 해당 timestamp를 가진 PetInfoModel 찾기
        val petInfoRef = database.child("pets").child(uid)
        petInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (petInfoSnapshot in snapshot.children) {
                    val petInfo = petInfoSnapshot.getValue(PetInfoModel::class.java)
                    if (petInfo?.timestamp == timestamp) {
                        // activePet 상태 반전
                        val updatedActivePet = !(petInfo.activePet ?: false)
                        petInfoSnapshot.ref.child("activePet").setValue(updatedActivePet)
                            .addOnSuccessListener {
                                // UI 업데이트
                                onCompleted()
                            }
                            .addOnFailureListener {
                                // 실패 처리
                            }
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 에러 처리
            }
        })
    }


    fun readPetinfodata(onCompleted: (MutableList<PetInfoModel>) -> Unit) {
        database.child("pets").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val petInfoList = mutableListOf<PetInfoModel>()
                    for (petInfoSnapshot in dataSnapshot.children) {
                        val petInfo = petInfoSnapshot.getValue(PetInfoModelDTO::class.java)
                        petInfo?.let {
                            it.key = petInfoSnapshot.key
                            petInfoList.add(it.toModel())
                        }
                    }
                    onCompleted(petInfoList) // 데이터 로드가 완료된 후 콜백 호출
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 에러 처리
                    databaseError.toException().printStackTrace()
                    onCompleted(mutableListOf()) // 에러 발생 시 빈 리스트를 반환
                }
            })
    }

    fun saveProfiledata(
        profile: ProfileModel,
        onCompleted: (ProfileModel?) -> Unit
    ) {
        database.child("profiles").child(uid).setValue(profile)
        onCompleted(profile)

    }

    fun readProfiledata(onCompleted: (ProfileModel?) -> Unit) {
        database.child("profiles").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val profile = dataSnapshot.getValue(ProfileModel::class.java)
                    onCompleted(profile) // 데이터 로드가 완료된 후 콜백 호출
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 에러 처리
                    databaseError.toException().printStackTrace()
                    onCompleted(null) // 에러 발생 시 null을 반환
                }
            })
    }

    fun readProfiledata(uid: String, onCompleted: (ProfileModel?) -> Unit) {
        database.child("profiles").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val profile = dataSnapshot.getValue(ProfileModel::class.java)
                    onCompleted(profile) // 데이터 로드가 완료된 후 콜백 호출
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 에러 처리
                    databaseError.toException().printStackTrace()
                    onCompleted(null) // 에러 발생 시 null을 반환
                }
            })
    }

    fun uploadImage(image: Uri?, onComplete: (String) -> Unit) {
        image?.let {
            // Firebase Storage 경로 설정
            val storageRef = FirebaseStorage.getInstance().reference
                .child("users")
                .child(uid)
                .child("images")
                .child("profile")
                .child("profile_${uid}" + "_" + "  ${Date()}.png")

            // 업로드 시작
            storageRef.putFile(image)
                .addOnSuccessListener { taskSnapshot ->
                    // 업로드 성공 시, 다운로드 URL 획득
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        onComplete(uri.toString()) // 업로드 성공과 URL 반환
                    }?.addOnFailureListener {
                        // URL 검색 실패
                    }
                }
                .addOnFailureListener {
                    // 업로드 실패
                }
        }
    }

    fun uploadImage(image: ByteArray, onComplete: (String) -> Unit) {
        // Firebase Storage 경로 설정
        val storageRef = FirebaseStorage.getInstance().reference
            .child("users")
            .child(uid)
            .child("images")
            .child("profile")
            .child("profile_${uid}_" + "_" + "  ${Date()}.png")

        // 업로드 시작
        storageRef.putBytes(image)
            .addOnSuccessListener { taskSnapshot ->
                // 업로드 성공 시, 다운로드 URL 획득
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    onComplete(uri.toString()) // 업로드 성공과 URL 반환
                }?.addOnFailureListener {
                    // URL 검색 실패
                }
            }
            .addOnFailureListener {
                // 업로드 실패
            }
    }


//    fun uploadSnapshotAndSaveWalkModel(googleMap: GoogleMap, walkModel: WalkModel) {
//        Log.d("Firebase", "uploadSnapshotAndSaveWalkModel은 잘 동작한답니다.")
//        CoroutineScope(Dispatchers.Main).launch {
//            try {
//                Log.d("Firebase", "uploadSnapshotAndSaveWalkModel은 잘 동작한답니다2.")
//                val snapshotUrl = uploadMapSnapshot(googleMap)
//                val updatedWalkModel = walkModel.copy(snapshotPath = snapshotUrl)
////                saveWalkList(updatedWalkModel)
//                Log.d("Firebase", "$updatedWalkModel")
//            } catch (e: Exception) {
//                // 에러 핸들링
//                e.printStackTrace()
//            }
//        }
//    }

//    private suspend fun uploadMapSnapshot(googleMap: GoogleMap): String =
//        withContext(Dispatchers.IO) {
//            val deferredUrl = CompletableDeferred<String>()
//
//            googleMap.snapshot { bitmap ->
//                // 메모리에 존재하는 비트맵을 바이트 배열로 변환
//                ByteArrayOutputStream().use { baos ->
//                    bitmap?.compress(Bitmap.CompressFormat.PNG, 90, baos)
//                    val data = baos.toByteArray()
//
//                    // 스냅샷 이름을 정의 (예: 날짜와 시간을 사용)
//                    val snapshotName = "map_snapshot_${
//                        SimpleDateFormat("yyMMddHHmmss", Locale.KOREA).format(Date())
//                    }.png"
//
//                    // Firebase Storage에 이미지 업로드
//                    val storageRef =
//                        FirebaseStorage.getInstance().reference.child("snapshots/$snapshotName")
//                    storageRef.putBytes(data)
//                        .addOnSuccessListener { taskSnapshot ->
//                            // 업로드 성공 시 다운로드 URL 획득
//                            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
//                                deferredUrl.complete(uri.toString())
//                            }
//                        }
//                        .addOnFailureListener { exception ->
//                            // 업로드 실패 시 에러 핸들링
//                            exception.printStackTrace()
//                            deferredUrl.completeExceptionally(exception)
//                        }
//                }
//            }
//
//            return@withContext deferredUrl.await() // 업로드 URL 반환
//        }

//    fun uploadImage(context: Context, imageData: ByteArray, onUploadComplete: (String) -> Unit) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // Firebase Storage 경로 설정
//                val storageRef = FirebaseStorage.getInstance().reference
//                    .child("users")
//                    .child(uid)
//                    .child("images")
//                    .child("snapshot")
//                    .child("snapshot_${uid}" + "_" + "  ${Date()}.png")
//                storageRef.putBytes(imageData)
//                    .addOnSuccessListener { taskSnapshot ->
//                        // 업로드 성공 시 다운로드 URL 획득
//                        taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
//                            onUploadComplete(uri.toString())
//                        }
//                    }
//                    .addOnFailureListener { exception ->
//                        // 업로드 실패 시 에러 핸들링
//                        exception.printStackTrace()
//                    }
//            } catch (e: Exception) {
//                // 에러 핸들링
//                e.printStackTrace()
//            }
//        }
//    }

//    fun uploadImage(context: Context, imageUri: Uri?, onUploadComplete: (String) -> Unit) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                context.contentResolver.openInputStream(imageUri!!)?.use { inputStream ->
//                    // InputStream으로부터 바이트 배열을 가져옴
//                    val data = inputStream.readBytes()
//
//                    // Firebase Storage 경로 설정
//                    val storageRef =
//                        FirebaseStorage.getInstance().reference
//                            .child("users")
//                            .child(uid)
//                            .child("images")
//                            .child("profile")
//                            .child("profile_${uid}" + "_" + "  ${Date()}.png")
//
//                    storageRef.putBytes(data)
//                        .addOnSuccessListener { taskSnapshot ->
//
//                            // 업로드 성공 시 다운로드 URL 획득
//                            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
//                                onUploadComplete(uri.toString())
//                            }
//                        }
//                        .addOnFailureListener { exception ->
//                            Log.d("bbbb", "addOnFailureListener 실행")
//                            // 업로드 실패 시 에러 핸들링
//                            exception.printStackTrace()
//                        }
//                } ?: throw IOException("Cannot open image Uri")
//            } catch (e: Exception) {
//                // 에러 핸들링
//                e.printStackTrace()
//            }
//        }
//    }

//    fun uploadUri(imageUri: Uri?, onComplete: (String) -> Unit) {
//        imageUri?.let {
//            // Firebase Storage 경로 설정
//            val storageRef = FirebaseStorage.getInstance().reference
//                .child("users")
//                .child(uid)
//                .child("images")
//                .child("profile")
//                .child("profile_${uid}" + "_" + "  ${Date()}.png")
//
//            // 업로드 시작
//            storageRef.putFile(imageUri)
//                .addOnSuccessListener { taskSnapshot ->
//                    // 업로드 성공 시, 다운로드 URL 획득
//                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
//                        onComplete(uri.toString()) // 업로드 성공과 URL 반환
//                    }?.addOnFailureListener {
//                        // URL 검색 실패
//                    }
//                }
//                .addOnFailureListener {
//                    // 업로드 실패
//                }
//        }
//    }

    //    fun uploadSnapshot(uid: String, googleMap: GoogleMap, onComplete: (Boolean) -> Unit) {
//        googleMap.snapshot { bitmap ->
//            // 비트맵을 바이트 배열로 변환
//            val baos = ByteArrayOutputStream()
//            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, baos)
//            val data = baos.toByteArray()
//
//            // Firebase Storage에 업로드
//            val storageRef = FirebaseStorage.getInstance().reference
//            val snapshotRef = storageRef.child("${uid}/images/map_snapshot/${Date()}.png")
//
//            val uploadTask = snapshotRef.putBytes(data)
//            uploadTask.addOnSuccessListener {
//                // 업로드 성공
//                onComplete(true)
//            }.addOnFailureListener {
//                // 업로드 실패
//                onComplete(false)
//            }
//        }
//    }
//
    fun Context.uploadImages(
        imageUris: MutableList<Uri>,
        onUploadComplete: (MutableList<String>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val urls = mutableListOf<String>()

            // 각 이미지 URI에 대해 반복
            for (imageUri in imageUris) {
                try {
                    val fileName =
                        "image_${SimpleDateFormat("yyMMddHHmmss", Locale.KOREA).format(Date())}.png"
                    val storageRef =
                        FirebaseStorage.getInstance().reference.child("images/$fileName")

                    // InputStream을 통해 이미지 데이터 가져오기
                    this@uploadImages.contentResolver.openInputStream(imageUri)
                        ?.use { inputStream ->
                            val data = inputStream.readBytes()
                            val uploadTask = storageRef.putBytes(data)

                            // 동기화를 위해 await() 사용, 비동기로 바꿔야 하면 이 부분을 수정
                            val taskSnapshot = uploadTask.await()
                            val downloadUrl = taskSnapshot.metadata?.reference?.downloadUrl?.await()

                            downloadUrl?.let {
                                urls.add(it.toString())
                            }
                        } ?: throw IOException("Cannot open image Uri")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 모든 이미지 업로드 후 콜백 호출
            withContext(Dispatchers.Main) {
                onUploadComplete(urls)
            }
        }
    }
}