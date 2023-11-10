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

    // Firebase 에서 불러 오거나 저장 할 데이터 의 타입을 지정 하는 상수
    const val TYPE_WALK = 1
    const val TYPE_MARKER = 2
    const val TYPE_PROFILE = 3

    // Firebase 에서 불러온 데이터 의 화면 binding 여부를 나타 내는 상수
    const val BIND_ON = 1
    const val BIND_OFF = 2

    // Firebase 에 데이터 를 업로드 할 때 CRUD 여부를 나타 내는 상수
    const val CREATE = 1
    const val READ = 2
    const val UPDATE = 3
    const val DELETE = 4
}