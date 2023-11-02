package com.footprint.app.api.model

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PostModel(
//    val profileImageUrl: Uri? = null,    // 프로필 사진 URL
    val nickname: String = "내새끼",           // 닉네임
    val postDate: String = SimpleDateFormat("yy년 MM월 dd일", Locale.KOREA).format(Date()),// 글 작성 일자
    val title: String = "",            // 글 제목
    val content: String = "",            // 글 내용
//    val postImageUrl: MutableList<ImageModel> = mutableListOf(),     // 게시글 의 사진 URL 리스트
    val likesCount: Int = 0,            // 좋아요 수
    val commentsCount: Int = 0,          // 댓글 수
    val tag: List<String> = listOf("Hot")      // 태그(여러개 의 리스트 를 가질 수 있어서 리스트 로 선언)
)