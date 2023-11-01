package com.footprint.app.api.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PostModel(
    val profileImageUrl: String? = null,    // 프로필 사진 URL
    val nickname: String = "내새끼",           // 닉네임
    val postDate: String = SimpleDateFormat("yy년 MM월 dd일", Locale.KOREA).format(Date()),// 글 작성 일자
    val title: String,            // 글 제목
    val content: String,            // 글 내용
    val postImageUrl: String? = null,      // 게시글 의 사진 URL (게시글 에 사진이 없을 수 있어서 Nullable)
    val likesCount: Int,            // 좋아요 수
    val commentsCount: Int,          // 댓글 수
    val tag: List<String> = listOf("Hot")      // 태그(여러개 의 리스트 를 가질 수 있어서 리스트로 선언)
)