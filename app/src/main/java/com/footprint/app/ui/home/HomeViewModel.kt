package com.footprint.app.ui.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.footprint.app.BuildConfig.GOOGLE_MAPS_API_KEY
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    // GoogleMap 인스턴스를 참조하기 위한 변수
    lateinit var mGoogleMap: GoogleMap

    // FusedLocationProviderClient가 위치 업데이트를 수신할 때 호출되는 콜백을 정의하는 데 사용되는 변수
    lateinit var locationCallback: LiveData<LocationCallback>

    // 기기 위치 정보를 가져오는 여러 메서드를 제공하는 FusedLocationProviderClient 인스턴스를 참조하기 위한 변수
    lateinit var fusedLocationClient: LiveData<FusedLocationProviderClient>

    // Polyline 인스턴스를 참조하기 위한 변수
    lateinit var path: LiveData<Polyline>

    private var _locationPermission = MutableLiveData<ActivityResultLauncher<Array<String>>>()
    var locationPermission: LiveData<ActivityResultLauncher<Array<String>>> = _locationPermission

    // 사용자의 이동 경로 저장
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
    fun getplaces() {
        // 입력값으로 keyword, type 넣기
        fetchplaces(null) // 초기 호출은 토큰 없이
    }
    fun fetchplaces(nextToken: String?){
        val placeitems = ArrayList<PlaceModel>()
//        NetWorkClient.apiService.getplace(keyword.value!!,location.value!!,radius.value!!,type.value!!,GOOGLE_MAPS_API_KEY,nextPageToken
        NetWorkClient.apiService.getplace("병원","${37.566610},${126.978403}",50000,"",GOOGLE_MAPS_API_KEY,nextpagetoken
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
                                    val type = item.types
                                    val keyword = "동물 병원"
                                    val nextpage = it.next_page_token
//                                    val place = PlaceModel(
//                                        location,
//                                        type,
//                                        keyword,
//                                        nextpage,
//                                    )

//                                     중복 체크
//                                    if (!placeitems.contains(place)) {
//                                        placeitems.add(place)
//                                    }
                                }
                                Log.d("FootprintApp", "장소 API 잘받아와짐")
                                Log.d("FootprintApp", "${placeitems}")
                                Log.d("FootprintApp", "${nextpagetoken}")
                                it.next_page_token?.let { token ->
                                    // next_page_token이 존재하면 약 2초의 딜레이 후 추가 요청을 합니다.
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        fetchplaces(token)
                                    }, 2000)
                                }
//                                _commentitem.value?.clear()
//                                _commentitem.value?.addAll(commentItems)
//                                _commentitem.postValue(_commentitem.value)
//                                Log.d("FootprintApp", "${_commentitem.value?.size}")
//                                Log.d("FootprintApp", "${_commentitem.value}")
                            }
                        }
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
