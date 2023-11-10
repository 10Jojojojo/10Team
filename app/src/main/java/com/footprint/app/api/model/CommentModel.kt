package com.footprint.app.api.model

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CommentModel(
    var postKey: String? = null, // Firebase에 업로드할때는 빈값으로 저장
    var commentKey:String? = null, // Firebase에 업로드할때는 빈값으로 저장하고, 받아올때 값을 할당한다. 이유는 commentKey를 토대로 수정과 삭제를 하기 위해서
    var uid: String = "",
    var timestamp: Long = System.currentTimeMillis(), // 댓글 작성 일자 시간 정보
    var content: String = "", // 댓글 내용
)