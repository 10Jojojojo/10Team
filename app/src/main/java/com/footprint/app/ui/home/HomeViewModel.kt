package com.footprint.app.ui.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.footprint.app.BuildConfig
import com.footprint.app.api.NetWorkClient
import com.footprint.app.api.model.PlaceModel
import com.footprint.app.api.serverdata.Location
import com.footprint.app.api.serverdata.PlaceData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    // 사용자의 이동 경로 저장
    // 이동 경로는 HomeFragment가 파괴되도 다른 Fragment에서 구글맵을 불러올 때 이용해야하니까, ViewModel에서 관리
    private var _pathPoints =
        MutableLiveData<MutableList<LatLng>>().apply { value = mutableListOf() }
    var pathPoints: LiveData<MutableList<LatLng>> = _pathPoints

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

    fun inputdata(mGoogleMap: GoogleMap, LatLng: MutableList<LatLng>, path: Polyline) {
        cameraPosition = mGoogleMap.cameraPosition // 현재 위치
        currentLatLng = cameraPosition.target // 카메라
        currentZoom = cameraPosition.zoom // 줌
        currentpathPoints = LatLng // 사용자의 이동 경로 저장
        currentpath = path // Polyline 객체
        Log.d("FootprintApp","저장된 데이터는요 현재위치 : ${cameraPosition}/n카메라 : ${currentLatLng}/n줌 : ${currentZoom}/n 사용자의 이동 경로 : ${currentpathPoints}/n PolyLine : ${currentpath}")
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
    fun getplaces(nextToken: String?,keyword:String,type:String) {

    NetWorkClient.apiService.getplace(
            keyword, "${37.566610},${126.978403}", 50000, type, BuildConfig.GOOGLE_MAPS_API_KEY, nextpagetoken

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
                                viewModelScope.launch(Dispatchers.IO) {it.next_page_token?.let { token ->
                                    // next_page_token이 존재하면 약 2초의 딜레이 후 추가 요청을 합니다.
                                    delay(2000)
                                    getplaces(token,keyword,type)
                                }}
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


 */