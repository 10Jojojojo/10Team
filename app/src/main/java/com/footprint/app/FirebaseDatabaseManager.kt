package com.footprint.app

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.footprint.app.api.model.CameraPositionDTO
import com.footprint.app.api.model.CommentModel
import com.footprint.app.api.model.FlagModel
import com.footprint.app.api.model.LatLngDTO
import com.footprint.app.api.model.PostModel
import com.footprint.app.api.model.ProfileModel
import com.footprint.app.api.model.ProfileModelDTO
import com.footprint.app.api.model.WalkModel
import com.footprint.app.api.model.WalkModelDTO
import com.footprint.app.api.model.toDTO
import com.footprint.app.api.model.toModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseDatabaseManager {
    private val database = Firebase.database.reference
    private val readPostState:String = "불러옴"

    // ProfileModel 저장
//    fun saveProfile(profile: ProfileModel) {
//        database.child("profiles").child(profile.nickName).setValue(profile)
//    }
    fun saveProfile(profile: ProfileModel) {
        val key = database.child("profiles").push().key  // 고유 키 생성
        if (key != null) {
            profile.key = key
            database.child("profiles").child(key).setValue(profile.toDTO())
        }
    }
    fun readProfile(key: String, onProfileReceived: (ProfileModel?) -> Unit) {
        database.child("profiles").child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val profileDTO = dataSnapshot.getValue(ProfileModelDTO::class.java)
                val profile = profileDTO?.toModel()
                onProfileReceived(profile) // 데이터 로드가 완료된 후 콜백 호출
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 에러를 처리합니다.
                databaseError.toException().printStackTrace()
                onProfileReceived(null) // 에러 발생 시 null을 반환
            }
        })
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
//    fun saveWalkList(walk: WalkModel) {
//        val key = database.child("walks").push().key  // 고유 키 생성
//        if (key != null) {
//            database.child("walks").child(key).setValue(walk.toDTO())
//        }
//    }
    // WalkModel 저장
    fun readWalkList(localWalkList: MutableList<WalkModel>, onCompleted: () -> Unit)  {
        database.child("walks").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val walksList = mutableListOf<WalkModel>()
                for (walkSnapshot in dataSnapshot.children) {
                    val walkDTO = walkSnapshot.getValue(WalkModelDTO::class.java)
                    val walk = walkDTO?.toModel()
                    walk?.let {
                        walksList.add(it)
                    }
                }
                localWalkList.clear()
                localWalkList.addAll(walksList)
                onCompleted() // 데이터 로드가 완료된 후 콜백 호출
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 에러를 처리
                databaseError.toException().printStackTrace()
            }
        })
    }

    // PostModel 저장
    fun savePostList(post: PostModel) {
//        database.child("posts").setValue(post)
        val key = database.child("posts").push().key  // 고유 키 생성
        if (key != null) {
            post.key = key
            database.child("posts").child(key).setValue(post.toDTO())
        }
    }
    fun readPostList(localPostList: MutableList<PostModel>, onCompleted: () -> Unit) {
        database.child("posts").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val postsList = mutableListOf<PostModel>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(PostModel::class.java)
                    post?.let {
                        // 각 PostModel 객체에 대해 필요한 경우 여기서 초기화를 할 수 있다.
                        // 예를 들어, 만약 Firebase에서 빈 배열이 아니라 null로 반환될 경우 여기서 처리
                        // 빈 데이터를 Firebase에 업로드한 경우 Firebase에서 데이터를 받아올때, 아무 값을 가지지 않기때문에 여기서 초기화를 진행
                        if (it.postImageUrls == null) {
                            it.postImageUrls = mutableListOf()
                        }
                        if (it.commentsList == null) {
                            it.commentsList = mutableListOf()
                        }
                        postsList.add(it)
                    }
                }
                localPostList.clear()
                localPostList.addAll(postsList)
                onCompleted() // 데이터 로드가 완료된 후 콜백 호출
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 에러를 처
                databaseError.toException().printStackTrace()
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

//    fun uploadImageToStorage(uri: Uri, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
//        val storageRef = Firebase.storage.reference
//        val imageRef = storageRef.child("images/${uri.lastPathSegment}")
//        val uploadTask = imageRef.putFile(uri)
//
//        uploadTask.addOnSuccessListener {
//            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
//                onSuccess(downloadUri.toString())
//            }
//        }.addOnFailureListener { exception ->
//            onError(exception)
//        }
//    }
    fun uploadSnapshotAndSaveWalkModel(googleMap: GoogleMap, walkModel: WalkModel) {
        Log.d("Firebase","uploadSnapshotAndSaveWalkModel은 잘 동작한답니다.")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d("Firebase","uploadSnapshotAndSaveWalkModel은 잘 동작한답니다2.")
                val snapshotUrl = uploadMapSnapshot(googleMap)
                val updatedWalkModel = walkModel.copy(snapshotPath = snapshotUrl)
                saveWalkList(updatedWalkModel)
                Log.d("Firebase","$updatedWalkModel")
            } catch (e: Exception) {
                // 에러 핸들링
                e.printStackTrace()
            }
        }
    }

    private suspend fun uploadMapSnapshot(googleMap: GoogleMap): String =
        withContext(Dispatchers.IO) {
            val deferredUrl = CompletableDeferred<String>()

            googleMap.snapshot { bitmap ->
                // 메모리에 존재하는 비트맵을 바이트 배열로 변환
                ByteArrayOutputStream().use { baos ->
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 90, baos)
                    val data = baos.toByteArray()

                    // 스냅샷 이름을 정의 (예: 날짜와 시간을 사용)
                    val snapshotName = "map_snapshot_${
                        SimpleDateFormat("yyMMddHHmmss", Locale.KOREA).format(Date())
                    }.png"

                    // Firebase Storage에 이미지 업로드
                    val storageRef =
                        FirebaseStorage.getInstance().reference.child("snapshots/$snapshotName")
                    storageRef.putBytes(data)
                        .addOnSuccessListener { taskSnapshot ->
                            // 업로드 성공 시 다운로드 URL 획득
                            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                                deferredUrl.complete(uri.toString())
                            }
                        }
                        .addOnFailureListener { exception ->
                            // 업로드 실패 시 에러 핸들링
                            exception.printStackTrace()
                            deferredUrl.completeExceptionally(exception)
                        }
                }
            }

            return@withContext deferredUrl.await() // 업로드 URL 반환
        }
    fun uploadImageToFirebaseStorage(context: Context, imageUri: Uri?, onUploadComplete: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                context.contentResolver.openInputStream(imageUri!!)?.use { inputStream ->
                    // InputStream으로부터 바이트 배열을 가져옴
                    val data = inputStream.readBytes()

                    // 파일 이름을 정의 (예: 날짜와 시간을 사용)
                    val fileName = "profile_image_${
                        SimpleDateFormat("yyMMddHHmmss", Locale.KOREA).format(Date())
                    }.png"

                    // Firebase Storage 경로 설정
                    val storageRef = FirebaseStorage.getInstance().reference.child("images/$fileName")
                    storageRef.putBytes(data)
                        .addOnSuccessListener { taskSnapshot ->
                            // 업로드 성공 시 다운로드 URL 획득
                            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                                onUploadComplete(uri.toString())
                            }
                        }
                        .addOnFailureListener { exception ->
                            // 업로드 실패 시 에러 핸들링
                            exception.printStackTrace()
                        }
                } ?: throw IOException("Cannot open image Uri")
            } catch (e: Exception) {
                // 에러 핸들링
                e.printStackTrace()
            }
        }
    }
    fun uploadImagesToFirebaseStorage(
        context: Context,
        imageUris: List<Uri>,
        onUploadComplete: (List<String>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val urls = mutableListOf<String>()

            // 각 이미지 URI에 대해 반복
            for (imageUri in imageUris) {
                try {
                    val fileName = "image_${SimpleDateFormat("yyMMddHHmmss", Locale.KOREA).format(Date())}.png"
                    val storageRef = FirebaseStorage.getInstance().reference.child("images/$fileName")

                    // InputStream을 통해 이미지 데이터 가져오기
                    context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
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

    private fun saveWalkList(walk: WalkModel) {
        val key = database.child("walks").push().key
        if (key != null) {
            walk.key = key
            database.child("walks").child(key).setValue(walk.toDTO()).addOnSuccessListener {
                // 성공적으로 데이터가 저장됨
            }.addOnFailureListener {
                // 데이터 저장에 실패함
            }
        }
    }
    fun addCommentToFirebase(postId: String, newComment: CommentModel,context: Context) {
        // 파이어베이스에 저장된 해당 게시글의 댓글 리스트를 가져온 후 댓글 추가
        val postRef = database.child("posts").child(postId)
        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                // 현재 데이터를 PostModel로 변환
                val post = currentData.getValue(PostModel::class.java)
                if (post == null) {
                    return Transaction.success(currentData)
                } else {
                    // 댓글 리스트에 새 댓글 추가
                    val comments = post.commentsList.toMutableList()
                    comments.add(newComment)
                    post.commentsList = comments
                    post.commentsCount = comments.size

                    // 변경된 post 객체를 다시 설정
                    currentData.value = post
                    return Transaction.success(currentData)
                }
            }

            override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                // 트랜잭션이 완료된 후 호출
                if (committed) {
                    // UI 업데이트 또는 성공 메시지 표시
                    Toast.makeText(context, "댓글이 추가되었습니다.", Toast.LENGTH_SHORT).show()
//                    // 댓글 입력란 클리어
//                    binding.etAddComment.text.clear()
//                    // 댓글 어댑터 업데이트
//                    communityAdapter.notifyDataSetChanged()
                } else {
                    // 실패 메시지 표시
                    Toast.makeText(context, "댓글 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

}