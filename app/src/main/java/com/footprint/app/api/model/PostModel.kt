package com.footprint.app.api.model

//data class PostModel(
//    val profileImageUrl: Uri? = null,    // 프로필 사진 URL
//    val nickname: String = "내새끼",           // 닉네임
//    val postDate: String = "", // 글 작성 일자
//    val title: String = "",            // 글 제목
//    val content: String = "",            // 글 내용
//    val postImageUrl: MutableList<ImageModel> = mutableListOf(),     // 게시글 의 사진 URL 리스트
//    val likesCount: Int = 0,            // 좋아요 수
//    val commentsCount: Int = 0,          // 댓글 수
//    val commentsList:List<CommentModel> = listOf(),
//    val tag: List<String> = listOf("Hot")      // 태그(여러개 의 리스트 를 가질 수 있어서 리스트 로 선언)
//    // Firebase의 Storage 경로를 String으로 저장해주어야함 근데 그거보다는,
//    // Post를 먼저 올리고, Firebase에 업로드하면, id값이 생길텐데, 이 id값을 이용해서 Storage에 디렉토리를 만들어주는것.
//    // 그리고 그 디렉토리 안에다가 이미지를 여러장 업로드를 하는것
//)

data class PostModel(
    var key:String = "", // 게시글의 고유 식별자
    val authorId: String = "", // 작성자의 ID
    val authorNickname: String = "", // 작성자의 닉네임
    val authorProfileImageUrl: String = "", // 작성자의 프로필 사진 URL
    val postDate: String = "", // = SimpleDateFormat("yy년 MM월 dd일", Locale.KOREA).format(Date()), // 글 작성 일자
    val title: String = "", // 글 제목
    val content: String = "", // 글 내용
    var postImageUrls: List<String> = mutableListOf(), // 게시글의 사진 URL 리스트
    val likesCount: Int = 0, // 좋아요 수
    var commentsCount: Int = 0, // 댓글 수
    var commentsList: MutableList<CommentModel> = mutableListOf(), // 댓글 리스트
    val tags: List<String> = listOf("Hot") // 태그 리스트
)
data class PostModelDTO(
    var key: String? = null,
    var postId: String? = null,
    var authorId: String? = null,
    var authorNickname: String? = null,
    var authorProfileImageUrl: String? = null,
    var postDate: String? = null,
    var title: String? = null,
    var content: String? = null,
    var postImageUrls: List<String>? = null,
    var likesCount: Int? = 0,
    var commentsCount: Int? = 0,
    var commentsList: List<CommentModelDTO>? = null,
    var tags: List<String>? = null
)
fun PostModel.toDTO(): PostModelDTO {
    return PostModelDTO(
        key = this.key,
        authorId = this.authorId,
        authorNickname = this.authorNickname,
        authorProfileImageUrl = this.authorProfileImageUrl,
        postDate = this.postDate,
        title = this.title,
        content = this.content,
        postImageUrls = this.postImageUrls,
        likesCount = this.likesCount,
        commentsCount = this.commentsCount,
        commentsList = this.commentsList.map { it.toDTO() },
        tags = this.tags
    )
}

fun PostModelDTO.toModel(): PostModel {
    return PostModel(
        key = this.key ?: "",
        authorId = this.authorId ?: "",
        authorNickname = this.authorNickname ?: "",
        authorProfileImageUrl = this.authorProfileImageUrl ?: "",
        postDate = this.postDate ?: "",
        title = this.title ?: "",
        content = this.content ?: "",
        postImageUrls = this.postImageUrls?.toList() ?: listOf(), // 바로 MutableList<String?>로 변환
        likesCount = this.likesCount ?: 0,
        commentsCount = this.commentsCount ?: 0,
//        commentsList = this.commentsList?.toMutableList() { it.toModel() } ?: mutableListOf(),
        commentsList = this.commentsList?.map { it.toModel() }?.toMutableList() ?: mutableListOf(),
        tags = this.tags ?: listOf("Hot")
    )
}