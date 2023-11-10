package com.footprint.app.api.model

import java.util.Date

data class PostModel(
    var postKey: String? = null, // 서버에서 데이터를 받을 때 설정
    val timestamp: Long = System.currentTimeMillis(), // binding할때 format을 해준다. System.currentTimeMillis()와 Date().time은 결국같지만 Date() 쪽이 객체를 생성하기때문에 쪼끔 느림
    var title: String = "", // 글 제목
    var content: String = "", // 글 내용
    var postImageUrls: MutableList<String> = mutableListOf(), // 게시글의 사진 URL 리스트
    var uid: String? = null, // 해당 게시글의 수정 권한을 주기위해 작성자의 uid를 받아온다.
    var nickname: String? = null, // uid를 통해 해당 게시글의 작성자 정보를 불러온다.
    val profileImageUri: String? = null, // uid를 통해 해당 게시글의 프로필 url을 받아온다.
    var commentCount: Long = 0L, // 댓글의 수는 PostModel에도 저장한다. 바로 화면에 표시하기 위해서
    var likeCount: Long = 0L, // 좋아요의 수는 PostModel에도 저장한다. 바로 화면에 표시하기 위해서
    var comments: MutableList<CommentModel> = mutableListOf(), // 게시글의 댓글리스트. 해당 게시글의 postKey로 CommentList를 받아온다.
    var likes: Map<String,Long> = emptyMap() // 게시글의 좋아요 리스트. id와 timestamp로 이루어져 있다. 해당 게시글의 postKey로 LikeMap을 받아온다.
)

// DTO 클래스 정의
data class PostModelDTO(
    var postKey: String? = null, // 서버에서 데이터를 받을 때 설정
    var timestamp: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var postImageUrls: MutableList<String>? = null,
    var uid: String? = null, // 해당 게시글의 수정 권한을 주기위해 작성자의 uid를 파이어베이스에 업로드한다.
    var nickname: String? = null, // uid를 통해 해당 게시글의 작성자 정보를 불러온다.
    var profileImageUri: String? = null, // uid를 통해 해당 게시글의 프로필 url을 받아온다.
    var commentCount: Long? = null, // 댓글의 수는 PostModel에도 저장한다. 바로 화면에 표시하기 위해서
    var likeCount: Long? = null, // 좋아요의 수는 PostModel에도 저장한다. 바로 화면에 표시하기 위해서
//    var comments: MutableList<CommentModel>? = null, // 게시글의 댓글리스트. 서버에는 업로드한다.
    var comments: Map<String, CommentModel>? = null,
    var likes: Map<String,Long>? = null// 게시글의 좋아요 리스트. 서버에는 업로드한다.
)

// 프로필 추가
fun PostModel.toProfile(profile:ProfileModel): PostModel {
    return PostModel(
        postKey = this.postKey,
        timestamp = this.timestamp,
        title = this.title,
        content = this.content,
        postImageUrls = this.postImageUrls,
        nickname = profile.nickName,
        profileImageUri = profile.profileImageUri,
        commentCount = this.commentCount,
        likeCount = this.likeCount,
        comments = this.comments,
        likes = this.likes
    )
}
// 모델에서 DTO로 변환
fun PostModel.toDTO(): PostModelDTO {
    // List의 각 요소를 Map의 Entry로 변환 (인덱스 또는 키를 자동 생성하거나 관리 필요)
    val commentsMap = this.comments.associateBy { it.commentKey?: "" } // commentKey는 각 댓글의 고유 ID

    return PostModelDTO(
        postKey = this.postKey,
        timestamp = this.timestamp,
        title = this.title,
        content = this.content,
        postImageUrls = this.postImageUrls,
        nickname = this.nickname,
        profileImageUri = this.profileImageUri,
        commentCount = this.commentCount,
        likeCount = this.likeCount,
//        comments = this.comments,
        comments = commentsMap ?: emptyMap(),
        likes = this.likes
    )
}

// DTO에서 모델로 변환
fun PostModelDTO.toModel(): PostModel {
    return PostModel(
        postKey = this.postKey,
        timestamp = this.timestamp ?: Date().time,
        title = this.title ?: "",
        content = this.content ?: "",
        postImageUrls = this.postImageUrls ?: mutableListOf(),
        nickname = this.nickname ?: "",
        profileImageUri = this.profileImageUri ?: "",
        commentCount = this.commentCount ?: 0L,
        likeCount = this.likeCount ?: 0L,
//        comments = this.comments ?: mutableListOf(),
        comments = this.comments?.values?.toMutableList() ?: mutableListOf(),
        likes = this.likes ?: emptyMap()

    )
}


fun PostModelDTO.toModel(profile: ProfileModel?): PostModel {
    // comments Map을 List로 변환
    val commentsList = this.comments?.values?.toMutableList() ?: mutableListOf()

    return PostModel(
        postKey = this.postKey,
        timestamp = this.timestamp ?: System.currentTimeMillis(),
        title = this.title.orEmpty(),
        content = this.content.orEmpty(),
        postImageUrls = this.postImageUrls ?: mutableListOf(),
        uid = this.uid,
        nickname = profile?.nickName,
        profileImageUri = profile?.profileImageUri,
        commentCount = this.commentCount ?: 0L,
        likeCount = this.likeCount ?: 0L,
        comments = commentsList, // 여기에 변환된 리스트 사용
        likes = this.likes ?: emptyMap()
    )
}