package com.footprint.app.api.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CommentModel(
    var key:String = "",
    var commentId: String = "",         // 댓글의 고유 식별자
    var authorId: String = "",          // 작성자의 ID
    var authorNickname: String = "",    // 작성자의 닉네임
    var authorProfileImageUrl: String? = "", // 작성자의 프로필 이미지 URL (null 가능)
    var postDate: String = SimpleDateFormat("yy년 MM월 dd일", Locale.KOREA).format(Date()), // 댓글 작성 일자
    var content: String = "",           // 댓글 내용
    var imageUrl: String? = "",         // 댓글에 첨부된 이미지의 URL (null 가능)
    var likesCount: Int = 0       // 좋아요 수 (기본값 0)
)
data class CommentModelDTO(
    var key: String? = null,
    var commentId: String? = null,
    var authorId: String? = null,
    var authorNickname: String? = null,
    var authorProfileImageUrl: String? = null,
    var postDate: String? = null,
    var content: String? = null,
    var imageUrl: String? = null,
    var likesCount: Int? = 0
)

fun CommentModel.toDTO(): CommentModelDTO {
    return CommentModelDTO(
        key = this.key,
        commentId = this.commentId,
        authorId = this.authorId,
        authorNickname = this.authorNickname,
        authorProfileImageUrl = this.authorProfileImageUrl,
        postDate = this.postDate,
        content = this.content,
        imageUrl = this.imageUrl,
        likesCount = this.likesCount
    )
}

fun CommentModelDTO.toModel(): CommentModel {
    return CommentModel(
        key = this.key ?: "",
        commentId = this.commentId ?: "",
        authorId = this.authorId ?: "",
        authorNickname = this.authorNickname ?: "",
        authorProfileImageUrl = this.authorProfileImageUrl ?: "",
        postDate = this.postDate ?: "",
        content = this.content ?: "",
        imageUrl = this.imageUrl ?: "",
        likesCount = this.likesCount ?: 0
    )
}