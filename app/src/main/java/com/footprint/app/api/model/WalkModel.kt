package com.footprint.app.api.model

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WalkModel(
    val distance:String,
    val walktime:String,
//    val address:String,,
    val pathpoint:MutableList<MutableList<LatLng>>,
    val currentLocation: CameraPosition,
    var snapshotPath: String? = null,  // 스냅샷 파일의 경로 저장
    val name:String = "나중에 설정할꺼에요",
    var starttime:String = "",
    var endtime:String = "",
    val date:String = SimpleDateFormat("yy년MM월dd일", Locale.KOREA).format(Date()),
    val dateid: String = SimpleDateFormat("yyMMddHHmmss", Locale.KOREA).format(Date())
)
