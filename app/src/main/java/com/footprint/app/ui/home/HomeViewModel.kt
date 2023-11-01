package com.footprint.app.ui.home


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.footprint.app.api.model.FlagModel
import com.footprint.app.api.model.WalkModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.pow

class HomeViewModel : ViewModel() {

    // 사용자 의 이동 경로 저장
    // 이동 경로는 HomeFragment 가 파괴 되도 다른 Fragment 에서 구글 맵을 불러올 때 이용 해야 하니까, ViewModel 에서 관리
    private val _pathPoints = MutableLiveData<MutableList<MutableList<LatLng>>>().apply {
        value = mutableListOf(mutableListOf())
    }
    val pathPoints: LiveData<MutableList<MutableList<LatLng>>> = _pathPoints
//    private var tokenization: String = ""
//
//    val placeItems = ArrayList<PlaceModel>()

    private var startTime: Long = 0L // ms로 반환
    private var endTime: Long = 0L // ms로 반환
    private var walkTime: Long = 0L // ms로 반환
    private val _walkState = MutableLiveData<String>().apply { value = "산책종료" }
    val walkState: LiveData<String> = _walkState
    private val _time = MutableLiveData<String>().apply { value = "00:00" }
    val time: LiveData<String> = _time
    val flagList = mutableListOf<FlagModel>()
    val walkList = mutableListOf<WalkModel>()

    var lineWidthText = "10"
    var colorCode = "000000"

    private val _lineWidthTextData = MutableLiveData<Float?>()
    val lineWidthTextData: LiveData<Float?> = _lineWidthTextData

    private val _colorCodeData = MutableLiveData<String>()
    val colorCodeData: LiveData<String> = _colorCodeData
    fun getDistance(): Int {
        val radius = 6372.8 * 1000
        var c = 0.0
        pathPoints.value?.let { value ->
            for (i in 0..<value.size) {
                for (j in 0 until value[i].size - 1) {
                    val lat1 = value[i][j].latitude
                    val lon1 = value[i][j].longitude
                    val lat2 = value[i][j + 1].latitude
                    val lon2 = value[i][j + 1].longitude
                    val dLat = Math.toRadians(lat2 - lat1)
                    val dLon = Math.toRadians(lon2 - lon1)
                    val a = kotlin.math.sin(dLat / 2).pow(2.0) + kotlin.math.sin(dLon / 2)
                        .pow(2.0) * kotlin.math.cos(
                        Math.toRadians(lat1)
                    ) * kotlin.math.cos(
                        Math.toRadians(lat2)
                    )
                    c += 2 * kotlin.math.asin(kotlin.math.sqrt(a))
                }
            }
        }
        return (radius * c).toInt()
    }

    private fun getTime() {
        val dataFormat = SimpleDateFormat("mm : ss", Locale.KOREA)
        _walkState.value = "산책중"
        startTime = System.currentTimeMillis()
        viewModelScope.launch {
            while (walkState.value == "산책중") {
                endTime = System.currentTimeMillis() - startTime + walkTime
                _time.value = dataFormat.format(endTime)
                delay(100)
            }
        }
    }

    fun startWalk() {
        if (_walkState.value == "산책종료") {
            getTime()
        }
    }

    fun pauseWalk() {
        walkTime = endTime
        when (_walkState.value) {
            "산책 일시 정지" -> {
                _walkState.value = "산책중"
                pathPoints.value!!.add(mutableListOf())  // 새로운 경로 리스트 시작
                getTime()
            }

            "산책중" -> {
                _walkState.value = "산책 일시 정지"
            }
        }
    }

    fun endWalk() {
        _walkState.value = "산책종료"
        endTime = 0L
        walkTime = 0L
        _pathPoints.value = mutableListOf(mutableListOf())
        _time.value = "00:00"
    }
    fun updateColorCode(newText: String) {
        _colorCodeData.value = newText
    }
    fun updateWidth(newNumber: Float?) {
        _lineWidthTextData.value = newNumber
    }


//    fun getPlaces(nextToken: String?, keyword: String, type: String) {
//        Log.d("FootprintApp", "함수 실행은 됬어요")
//        NetWorkClient.apiService.getPlace(
//            keyword,
//            "${37.566610},${126.978403}",
//            50000,
//            type,
//            MainActivity.apiKey,
//            tokenization
//
//        ) // null 이 아님을 확인 후 실행 해야 될것 같다.
//            ?.enqueue(object : Callback<PlaceData?> {
//                override fun onResponse(
//                    call: Call<PlaceData?>,
//                    response: Response<PlaceData?>
//                ) {
//                    if (response.isSuccessful) {
//                        response.body()?.let {
//                            tokenization = it.next_page_token
//                            if (it.results.isNotEmpty()) {
//                                for (item in it.results) {
//                                    val location = item.geometry.location
//                                    val tokenization = it.next_page_token
//                                    val place = PlaceModel(
//                                        location,
//                                        type,
//                                        keyword,
//                                    )
//
////                                     중복 체크
//                                    if (!placeItems.contains(place)) {
//                                        placeItems.add(place)
//                                    }
//                                }
//                                viewModelScope.launch(Dispatchers.IO) {
//                                    it.next_page_token.let { token ->
//                                        // next_page_token 이 존재 하면 약 2초의 딜레이 후 추가 요청
//                                        delay(2000)
//                                        getPlaces(token, keyword, type)
//                                    }
//                                }
//                            }
//                        }
//                        Log.d("FootprintApp", "${placeItems}")
//                    } else {
//                        val errorBody = response.errorBody()?.string()
//                        Log.e("FootprintApp", "API 에러: $errorBody")
//                    }
//                }
//
//                override fun onFailure(call: Call<PlaceData?>, t: Throwable) {
//                    Log.e("FootprintApp", "에러 : ${t.message}")
//                }
//            })
//    }

}

/*
1. ViewModel 에서 관리할 변수와 Fragment 에서 관리할 변수 나누기
UI 뿐 아니라 UI update 요소도 Fragment 에서 관리 해야 한다.
[HomeFragment]
GoogleMap : 화면에 표시 되는 UI
LocationCallback : 위치 update 를 처리 하는 콜백. 위치 update 는 UI update
FusedLocationProviderClient : 위치 정보를 가져 오는 Client. 위치 정보를 기반 으로 UI update 발생
ActivityResultLauncher<Array<String>> : 위치 권한 요청. 위치는 위의 변수 들을 확인 하면 알 수 잇듯 UI 관련 요소
path: Polyline : 구글 맵에서 이동 경로를 표시 하는 선 UI

[HomeViewModel]
pathPoints: MutableList<LatLng> : 사용자 의 이동 경로 저장. 이동 경로는 Firebase 를 통해 저장 되어야 할 요소 이므로 Fragment 가 파괴된 후에도
남아 있어야 한다. 따라서 ViewModel 에서 관리 해야 한다.

2. 감시할 변수와 그렇지 않은 변수도 나눠야 될 것 같다는 생각이 든다.

타이머 예시
코루틴 안에 while 문을 이용 하여 delay 를 100을 주고, 산책 종료 버튼이 눌리기 전까지 계속 실행 되게끔
walkState 라는 변수를 선언 하고, while 에 조건을 걸어서 walkState 가 true 일때 반복, false 일때 반복이 해제 되게끔

저장할 시간 정하기

산책 시작
종료 시간 - 스타트 시간 = 산책 시간 저장

산책 일시 정지 & 재개
산책 일시 정지&재개 시 시간은 어떻게 할 것인지, 코루틴 스코프 는 어떻게 다시 돌릴 것인지 생각
저장한 산책 시간 만큼 더해서 재개 되도록 하기.

산책 종료

경로는 저장 하고
거리는 일부만 계속 더하기.
그리고 거리 더할때 일정 숫자 이상 이면 안 더해 지게끔 하면 될듯.
경로 할 때 어떻게 해야 하지.?
 */