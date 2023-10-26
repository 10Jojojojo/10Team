package com.footprint.app.api.model

import android.net.Uri
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WalkModel(
    val distance:String,
    val walktime:String,
    val pathpoint:MutableList<MutableList<LatLng>>,
    val currentLocation: CameraPosition,
    var snapshotPath: String? = null,  // 스냅샷 파일의 경로 저장
    var petimage: Uri? = null, // 산책한 반려동물 사진
    val name:String = "내새끼",
    var starttime:String = "",
    var endtime:String = "",
    val date:String = SimpleDateFormat("yy년 MM월 dd일", Locale.KOREA).format(Date()),
    val dateid: String = SimpleDateFormat("yyMMddHHmmss", Locale.KOREA).format(Date())
)

// 사용자의 사진도
// 년월일은 시작 시간 위에 사용자 이름 왼쪽 사용자 사진 오른쪽
// 산책 거리 라벨, 산책 시간 라벨
// 아이템 쪽 산책 거리 라벨, 산책 시간 라벨
// 아이템 쪽 사용자 이름, 사용자 사진, 산책 날짜