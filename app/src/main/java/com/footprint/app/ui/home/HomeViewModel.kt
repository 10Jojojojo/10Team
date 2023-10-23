package com.footprint.app.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.footprint.app.BuildConfig
import com.footprint.app.api.NetWorkClient
import com.footprint.app.api.model.PlaceModel
import com.footprint.app.api.serverdata.Location
import com.footprint.app.api.serverdata.PlaceData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Math.asin
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.sqrt
import java.text.SimpleDateFormat
import kotlin.math.pow

class HomeViewModel : ViewModel() {

    // 사용자의 이동 경로 저장
    // 이동 경로는 HomeFragment가 파괴되도 다른 Fragment에서 구글맵을 불러올 때 이용해야하니까, ViewModel에서 관리
    private val _pathPoints = MutableLiveData<MutableList<MutableList<LatLng>>>().apply {
        value = mutableListOf(mutableListOf())
    }
    val pathPoints: LiveData<MutableList<MutableList<LatLng>>> = _pathPoints

    lateinit var cameraPosition: CameraPosition // 현재 위치
    lateinit var currentLatLng: LatLng // 카메라
    var currentZoom: Float = 0f // 줌
    lateinit var currentpathPoints: MutableList<LatLng> // 사용자의 이동 경로 저장
    lateinit var currentpath: Polyline // Polyline 객체
    private var nextpagetoken: String = ""

    private var _keyword = MutableLiveData<String>()
    var keyword: LiveData<String> = _keyword

    private var _location = MutableLiveData<Location>()
    var location: LiveData<Location> = _location

    private var _radius = MutableLiveData<Int>()
    var radius: LiveData<Int> = _radius

    private var _type = MutableLiveData<String>()
    var type: LiveData<String> = _type

    val placeitems = ArrayList<PlaceModel>()

    var startTime: Long = 0L // ms로 반환
    var endTime: Long = 0L // ms로 반환
    var walkTime: Long = 0L // ms로 반환
    private val _walkstate = MutableLiveData<String>().apply { value = "산책종료" }
    val walkstate: LiveData<String> = _walkstate
    private val _time = MutableLiveData<String>().apply { value = "00:00" }
    val time: LiveData<String> = _time

    fun inputdata(mGoogleMap: GoogleMap, LatLng: MutableList<LatLng>, path: Polyline) {
        cameraPosition = mGoogleMap.cameraPosition // 현재 위치
        currentLatLng = cameraPosition.target // 카메라
        currentZoom = cameraPosition.zoom // 줌
        currentpathPoints = LatLng // 사용자의 이동 경로 저장
        currentpath = path // Polyline 객체
        Log.d(
            "FootprintApp",
            "저장된 데이터는요 현재위치 : ${cameraPosition}/n카메라 : ${currentLatLng}/n줌 : ${currentZoom}/n 사용자의 이동 경로 : ${currentpathPoints}/n PolyLine : ${currentpath}"
        )
    }

    fun outputdata(mGoogleMap: GoogleMap) {
        val newCameraPosition = CameraPosition.builder()
            .target(currentLatLng)
            .zoom(currentZoom)
            .build()
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))

        // Polyline 경로 설정
        currentpath.points = currentpathPoints
    }

    fun getDistance(): Int {
        val R = 6372.8 * 1000
        var c = 0.0
        for (i in 0..pathPoints.value!!.size-1) {
            for (j in 0 until pathPoints.value!![i].size - 1) {
                val lat1 = pathPoints.value!![i][j].latitude
                val lon1 = pathPoints.value!![i][j].longitude
                val lat2 = pathPoints.value!![i][j + 1].latitude
                val lon2 = pathPoints.value!![i][j + 1].longitude
                val dLat = Math.toRadians(lat2 - lat1)
                val dLon = Math.toRadians(lon2 - lon1)
                val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(Math.toRadians(lat1)) * cos(
                    Math.toRadians(lat2)
                )
                c = c + 2 * asin(sqrt(a))
            }
        }
        return (R * c).toInt()
    }

    fun getTime(){
        val dataFormat = SimpleDateFormat("mm:ss")
        _walkstate.value = "산책중"
        startTime = System.currentTimeMillis()
        viewModelScope.launch {
            while (walkstate.value == "산책중") {
                endTime = System.currentTimeMillis() - startTime + walkTime
                _time.value = dataFormat.format(endTime)
                delay(100)
            }
        }
    }
    fun startwalk() {
        if (_walkstate.value == "산책종료") {
            getTime()
        }
    }

    fun pausewalk() {
        walkTime = endTime
        when(_walkstate.value)
        {
            "산책일시정지" -> {
                _walkstate.value = "산책중"
                this.pathPoints.value?.let {
                    it.add(mutableListOf())  // 새로운 경로 리스트 시작
                }
                getTime()
            }
            "산책중" -> {
                _walkstate.value = "산책일시정지"
            }
        }
    }

    fun endwalk() {
        _walkstate.value = "산책종료"
        endTime = 0L
        walkTime = 0L
        _pathPoints.value = mutableListOf(mutableListOf())
        _time.value = "00:00"
    }


    fun getplaces(nextToken: String?, keyword: String, type: String) {

        NetWorkClient.apiService.getplace(
            keyword,
            "${37.566610},${126.978403}",
            50000,
            type,
            BuildConfig.GOOGLE_MAPS_API_KEY,
            nextpagetoken

        ) // null이 아님을 확인 후 실행해야 될것 같다.
            ?.enqueue(object : Callback<PlaceData?> {
                override fun onResponse(
                    call: Call<PlaceData?>,
                    response: Response<PlaceData?>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            nextpagetoken = it.next_page_token
                            if (!it.results.isNullOrEmpty()) {
                                for (item in it.results) {
                                    val location = item.geometry.location
                                    val nextpage = it.next_page_token
                                    val place = PlaceModel(
                                        location,
                                        type,
                                        keyword,
                                    )

//                                     중복 체크
                                    if (!placeitems.contains(place)) {
                                        placeitems.add(place)
                                    }
                                }
                                Log.d("FootprintApp", "장소 API 잘받아와짐")
                                Log.d("FootprintApp", "${nextpagetoken}")
                                viewModelScope.launch(Dispatchers.IO) {
                                    it.next_page_token?.let { token ->
                                        // next_page_token이 존재하면 약 2초의 딜레이 후 추가 요청을 합니다.
                                        delay(2000)
                                        getplaces(token, keyword, type)
                                    }
                                }
//                                _commentitem.value?.clear()
//                                _commentitem.value?.addAll(commentItems)
//                                _commentitem.postValue(_commentitem.value)
//                                Log.d("FootprintApp", "${_commentitem.value?.size}")
//                                Log.d("FootprintApp", "${_commentitem.value}")
                            }
                        }

                        Log.d("FootprintApp", "${placeitems}")
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("FootprintApp", "API 에러: $errorBody")
                    }
                }

                override fun onFailure(call: Call<PlaceData?>, t: Throwable) {
                    Log.e("FootprintApp", "에러 : ${t.message}")
                }
            })
    }

}

/*
1. ViewModel에서 관리할 변수와 Fragment에서 관리할 변수 나누기
UI 뿐 아니라 UI 업데이트 요소도 Fragment에서 관리해야 한다.
[HomeFragment]
GoogleMap : 화면에 표시되는 UI
LocationCallback : 위치 업데이트를 처리하는 콜백. 위치 업데이트는 UI 업데이트
FusedLocationProviderClient : 위치 정보를 가져오는 클라이언트. 위치 정보를 기반으로 UI 업데이트 발생
ActivityResultLauncher<Array<String>> : 위치 권한 요청. 위치는 위의 변수들을 확인하면 알 수 잇듯 UI 관련 요소
path: Polyline : 구글 맵에서 이동경로를 표시하는 선 UI

[HomeViewModel]
pathPoints: MutableList<LatLng> : 사용자의 이동 경로 저장. 이동 경로는 Firebase를 통해 저장되어야 할 요소이므로 Fragment가 파괴된 후에도
남아 있어야 한다. 따라서 ViewModel에서 관리해야 한다.

2. 감시할 변수와 그렇지 않은 변수도 나누어야 될것같다는 생각이 든다.

타이머 예시
코루틴 안에 while문을 이용하여 delay를 100을 주고, 산책 종료 버튼이 눌리기 전까지 계속 실행되게끔
walkstate 라는 변수를 선언하고, while에 조건을 걸어서 walkstate가 true일때 반복, false일때 반복이 해제되게끔

저장할 시간 정하기

산책 시작
종료시간 - 스타트 시간 = 산책시간 저장

산책 일시정지 & 재개
산책 일시정지&재개 시 시간은 어떻게 할것인지, 코루틴스코프는 어떻게 다시 돌릴것인지 생각
저장한 산책시간만큼 더해서 재개되도록 하기.

산책 종료

경로는 저장하고
거리는 일부만 계속 더하기.
그리고 거리 더할때 일정 숫자 이상이면 안더해지게끔 하면 될듯.
경로 할 때 어떻게 해야하지.?
 */