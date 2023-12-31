package com.footprint.app.ui.home


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.footprint.app.FirebaseDatabaseManager
import com.footprint.app.FirebaseDatabaseManager.readMarkerdata
import com.footprint.app.FirebaseDatabaseManager.readWalkdata
import com.footprint.app.FirebaseDatabaseManager.saveMarkerdata
import com.footprint.app.FirebaseDatabaseManager.saveProfiledata
import com.footprint.app.FirebaseDatabaseManager.saveWalkdata
import com.footprint.app.api.model.MarkerModel
import com.footprint.app.api.model.PetInfoModel
import com.footprint.app.api.model.PostModel
import com.footprint.app.api.model.ProfileModel
import com.footprint.app.api.model.WalkModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow

class HomeViewModel : ViewModel() {

    // 사용자 의 이동 경로 저장
    // 이동 경로는 HomeFragment 가 파괴 되도 다른 Fragment 에서 구글 맵을 불러올 때 이용 해야 하니까, ViewModel 에서 관리
    // 새로운 MutableList를 할당?해주는식으로 데이터 변화를 만들어도 될듯
    // add로 하면 Livedata로 감시가안됨
    private val _pathPoints = MutableLiveData<MutableList<MutableList<LatLng>>>().apply {
        value = mutableListOf(mutableListOf())
    }
    val pathPoints: LiveData<MutableList<MutableList<LatLng>>> = _pathPoints
//    private var tokenization: String = ""
//
//    val placeItems = ArrayList<PlaceModel>()

    var startTime: Long = 0L // ms로 반환
    var endTime: Long = 0L // ms로 반환
    private var walkTime: Long = 0L // ms로 반환
    private val _walkState = MutableLiveData<String>().apply { value = "산책종료" }
    val walkState: LiveData<String> = _walkState
    private val _time = MutableLiveData<Long>().apply { value = 0L }
    val time: LiveData<Long> = _time

    private var _walkList =
        MutableLiveData<MutableList<WalkModel>>().apply { readWalkdata { value = it } }
    val walkList: LiveData<MutableList<WalkModel>> = _walkList
    private var _markerList =
        MutableLiveData<MutableList<MarkerModel>>().apply { readMarkerdata { value = it } }
    val markerList: LiveData<MutableList<MarkerModel>> = _markerList

    // GoogleMap 준비 상태를 추적하는 LiveData
    private val _isMapReady = MutableLiveData<Boolean>().apply { value = false }
    val isMapReady: LiveData<Boolean> = _isMapReady

    var lineWidthText = "10"
    var colorCode = "000000"

    var distance: Int = 0

    private val _lineWidthTextData = MutableLiveData<Float?>()
    val lineWidthTextData: LiveData<Float?> = _lineWidthTextData

    private val _colorCodeData = MutableLiveData<String>()
    val colorCodeData: LiveData<String> = _colorCodeData

    private var isHomeObserve = false
    private var isHomeObserve2 = false

    var petList:MutableList<Long> = mutableListOf()
    private var _petInfoList = MutableLiveData<MutableList<PetInfoModel>>().apply {
        FirebaseDatabaseManager.readPetinfodata {
            value = it
            Log.d("aaaaaa1", "${petInfoList.value}")
        }
    }
    val petInfoList: LiveData<MutableList<PetInfoModel>> = _petInfoList
    private var _profile = MutableLiveData<ProfileModel>().apply {
        FirebaseDatabaseManager.readProfiledata {
            it?.let {
                value = it
            } ?: ProfileModel()
        }
    }
    val profile: LiveData<ProfileModel> = _profile
    private var _myPostList = MutableLiveData<MutableList<PostModel>>().apply {
        FirebaseDatabaseManager.readMyPostdata() { value = it
        }
    }
    val myPostList: LiveData<MutableList<PostModel>> = _myPostList
    fun updateProfile(profile: ProfileModel){
        saveProfiledata(profile) {
            _profile.value = it
        }
    }
    fun updatePetInfo(petInfo:PetInfoModel){
        FirebaseDatabaseManager.savePetinfodata(petInfo) {
            val currentList = _petInfoList.value ?: mutableListOf()

            currentList.add(petInfo)

            _petInfoList.value = currentList
        }
    }

    fun updateWalk(walk: WalkModel) {
        // 단순히 _walkList2.value?.add(walk) 로는 value는 바뀌지만, Livedata는 변경되지 않는다.
        // 따라서 새로운 List를 할당해야 Livedata도 변경되고, 그러기 위해서 var _walkList로 바꿔주었다.

        // 현재 MutableLiveData가 가지고 있는 값을 가져온다.
        val currentList = _walkList.value ?: mutableListOf()

        // 새로운 walk 객체를 리스트에 추가한다.
        currentList.add(walk)
        // 변경된 리스트를 MutableLiveData에 다시 설정한다.
        _walkList.value = currentList
        saveWalkdata(walk){}
    }

    fun updateMarker(marker: MarkerModel) {
        val currentList = _markerList.value ?: mutableListOf()

        currentList.add(marker)

        _markerList.value = currentList
        saveMarkerdata(marker){}
    }

    // GoogleMap이 준비되었을 때 이 메서드를 호출한다.
    fun setMapReady() {
        if (isMapReady.value == false) {
            _isMapReady.value = true
        }
    }

    fun setHomeObserve(set:Boolean) {
        isHomeObserve = set
    }

    fun getHomeObserve(): Boolean {
        return isHomeObserve
    }

    fun setHomeObserve2(set:Boolean) {
        isHomeObserve2 = set
    }

    fun getHomeObserve2(): Boolean {
        return isHomeObserve2
    }

    fun getDistance() {
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
        distance = (radius * c).toInt()
    }

    private fun getTime() {
        _walkState.value = "산책중"
        startTime = System.currentTimeMillis()
        // 이론적으로는 메모리가 부족하면 뷰가 먼저 파괴되고, 뷰의 라이프사이클이 파괴되면서 뷰모델도 제거가 되기떄문에, 서비스에 옮겨두는게 좋음
        // 시간되면 해보기(일단 문제없이 돌아가기는 하니까 인지만 하고 후순위로) 서비스에도 코루틴 사용가능 똑같이 그리고 디스패처도 지정해서
        // 전역변수로 코루틴 스코프 객체를 만들어서 하면 될듯
        // 구글링으로, 서비스에서 코루틴 사용방법 이런 키워드로 검색해보기
        // 시간계산의 경우 CPU 연산을 조금 사용해서, 디스패처는 디폴트로 사용하기
        viewModelScope.launch {
            while (walkState.value == "산책중") {
                endTime = System.currentTimeMillis() - startTime + walkTime
                _time.value = endTime
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
        _time.value = 0L
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