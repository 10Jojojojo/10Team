package com.footprint.app

object Constants {
    // 갤러리 사진 권한 요청에 사용 되는 상수
    const val REQUEST_GALLERY = 1
    const val REQUEST_PERMISSION = 2

    // CommunityAdapter 에서 ViewType 을 나눌 때 사용 되는 상수
    const val TYPE_POST = 1
    const val TYPE_TAG = 2
    const val TYPE_IMAGE = 3

    // Firebase 에서 데이터 를 불러 왔는 지 확인 하기 위한 상수
    const val STATUS_NOT_LOADED = 1
    const val STATUS_LOADED = 2
}