package com.footprint.app.api.model

import com.footprint.app.api.serverdata.Location

data class PlaceModel(
    val location: Location,
    val type: List<String>,
    val keyword: String,
    val nextpage: String
)
/*
일단은 마커 표시용으로 위도와 경도, 타입과 키워드만 가져오고
이후 추가적인 데이터를 표시할 때(여는시간, 닫는시간이나 건물 사진, 별점이나 그 외 등등) 더 추가해가자.
*/