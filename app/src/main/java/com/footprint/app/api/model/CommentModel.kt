package com.footprint.app.api.model

data class CommentModel(
    var postKey: String? = null, // Firebase에 업로드할때 저장
    var commentKey:String? = null, // Firebase에 업로드할때는 빈값으로 저장하고, 받아올때 값을 할당한다. 이유는 commentKey를 토대로 수정과 삭제를 하기 위해서
    var uid: String = "",
    var nickname: String? = null, // uid를 통해 해당 게시글의 작성자 정보를 불러온다.
    var profileImageUri: String? = null, // uid를 통해 해당 게시글의 프로필 url을 받아온다.
    var timestamp: Long = System.currentTimeMillis(), // 댓글 작성 일자 시간 정보
    var content: String = "", // 댓글 내용
)