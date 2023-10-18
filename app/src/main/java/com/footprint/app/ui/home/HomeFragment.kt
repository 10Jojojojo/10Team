package com.footprint.app.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.footprint.app.R
import com.footprint.app.api.model.PlaceModel
import com.footprint.app.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

// Fragment()에 R.layout.fragment_home를 입력으로 주면, 생성자 주입이라고 하는건데 (공식문서에는없음)
// onCreateView에서 하는동작을 대체해줌.
class HomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback, GoogleMap.OnPolylineClickListener,
    GoogleMap.OnPolygonClickListener {
    private var _binding: FragmentHomeBinding? = null
    // GoogleMap 인스턴스를 참조하기 위한 변수
    lateinit var mGoogleMap: GoogleMap

    // FusedLocationProviderClient가 위치 업데이트를 수신할 때 호출되는 콜백을 정의하는 데 사용되는 변수
    lateinit var locationCallback: LocationCallback

    // 기기 위치 정보를 가져오는 여러 메서드를 제공하는 FusedLocationProviderClient 인스턴스를 참조하기 위한 변수
    lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var locationPermission: ActivityResultLauncher<Array<String>>

    private var pathPoints = mutableListOf<LatLng>() // 사용자의 이동 경로 저장

    private lateinit var path: Polyline // Polyline 객체

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val homeViewModel by lazy {ViewModelProvider(this).get(HomeViewModel::class.java)}


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
//        val homeViewModel =
//            ViewModelProvider(this).get(HomeViewModel::class.java)
//        _binding = FragmentHomeBinding.inflate(inflater, container, false) // 64번째 줄에 있어서 없어도됨
//        val root: View = binding.root // 얘도 없어도됨
        // 온크리에이트 뷰에는 버튼이 들어가면 안됨. 온크리에이트뷰는 뷰를 생성하는과정(그ㅡ래서 return이 root인듯
        // 그래서 초기화안되고 불러와서 널값 인셉션? 그런 에러 발생확률 높음)
        // 온뷰크리에이트에서 바인딩관련된거 하기

//        val textView: TextView = binding.textHome
        initView()
        getmap()
        getPermission()
        observeViewModel() // 뷰를 먼저 생성하고
    }
    private fun observeViewModel(){
        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
        }
    }
    private fun initView(){ // private는 여기 이 프래그먼트에서만 쓸꺼라는거
        binding.testinputbutton.setOnClickListener {
            homeViewModel.inputdata(mGoogleMap,pathPoints,path)
        }
        binding.testoutputbutton.setOnClickListener {
//            homeViewModel.outputdata(mGoogleMap)
            placeMarkersOnMap(homeViewModel.placeitems)
        }
        binding.testapibutton.setOnClickListener {
            homeViewModel.getplaces(null,"병원","")
            homeViewModel.getplaces(null,"애견샾","")
        }
        binding.testpathlogbutton.setOnClickListener {
            Log.d("GoogleMapPractice","저장된 경로 : ${pathPoints}")
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 지도 프래그먼트를 가져와서 OnMapReadyCallback을 등록

    // 지도가 준비되었을 때 호출되는 콜백
    // 입력값은 childFragmentManager에서 입력하는 R.id.map_fragment값이 된다.
    override fun onMapReady(p0: GoogleMap) {
        val seoul = LatLng(37.566610, 126.978403)
        // 위치(위도와 경도)

        mGoogleMap = p0
        mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mGoogleMap.apply {
            val markerOptions = MarkerOptions()
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            // 마커 아이콘 하나 해서
            markerOptions.position(seoul)
            // 서울 위치에
            markerOptions.title("서울시청")
            // 이런
            markerOptions.snippet("Tel:01-120")
            // 정보들로
            addMarker(markerOptions)
            // 마커를 추가해줌. 이거는 현재위치와는 별개의 마커.
        }

        // 위치 정보를 받음 Activity가 아닌 Fragment라 this를 requireContext()로 수정
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Polyline 초기 설정
        path = mGoogleMap.addPolyline(PolylineOptions().color(Color.RED).width(10f))
        updateLocationUI()
    }

    // Polyline이 클릭되었을 때 호출되는 콜백
    override fun onPolylineClick(p0: Polyline) {
    }

    // Polygon이 클릭되었을 때 호출되는 콜백
    override fun onPolygonClick(p0: Polygon) {
    }
    fun getPermission(){
        // 이 아래의 21의 줄이 있어야 onMapReady가 콜백이 되고, updateLocationUI()가 동작하는것 같음.
        locationPermission = // 2. Permissions()를 검사한 후, results를 입력으로 주고, 그 결과가 만약 권한이 있다면,
                // .xml의 Fragment에 this를 연결시킴
                // 그 this는 GoogleMap
                // OnMapReadyCallback을 상속받는데, 이게 추가되면 override 메서드로 onMapReady가 필수로 들어가야함
                // onMapReadyCallback에 의해서 Map이 준비가되면 자동으로 onMapReady가 콜백이 됨.
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
                if (results.all { it.value }) {
                    (childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment)!!.getMapAsync(
                        this
                    ) // 프래그먼트를
                } else {
                    Toast.makeText(requireContext(), "권한 승인이 필요합니다.", Toast.LENGTH_LONG).show()
                }
            }
        // 권한이 없으면 권한을 요청해야함
        locationPermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
    fun getmap(){
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

    }

    // 위치 관련 UI 업데이트 메서드
    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        try {
            val locationRequest = LocationRequest.create().apply {
                interval = 1000 // 1초에 1번씩
                fastestInterval = 500
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            } //
            locationCallback = object : LocationCallback() { // LocationCallback()이라는 익명 객체를 생성하고,
                override fun onLocationResult(locationResult: LocationResult) { // 1초에 1번씩 하는걸 입력으로 넣어줌
                    locationResult?.let { //
                        for (location in it.locations) {
//                            Log.d(
//                                "GoogleMapPractice",
//                                "위도 : ${location.latitude} 경도: ${location.longitude}"
//                            )
                            setLastLocation(location) // 현재 위치가 갱신이 되면(LocationCallback() 때문인듯),
                            // 현재 위치를 setLastLocation에 입력으로 주며 호출
                        }
                    }
                }
            }

            // 입력으로 위에서 만들어준 locationRequest와, 콜백을 넣어줌. 그러면
            // 위치가 바뀔때, 초당 1번씩 locationCallback이 불리게 되는듯?
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.myLooper()!!
            )
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun setLastLocation(location: Location) {
        val newLatLng = LatLng(location.latitude, location.longitude)
        pathPoints.add(newLatLng) // 위치 데이터를 리스트에 저장
        path.points = pathPoints // Polyline 업데이트
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15f)) // 카메라를 현재 위치로 이동
    }
    fun placeMarkersOnMap(places: List<PlaceModel>) {
        for (place in places) {
            val location = LatLng(place.location.lat, place.location.lng)
            val markerOptions = MarkerOptions()
                .position(location)
//                .title(place.name)
//                .snippet(place.address)
                .icon(BitmapDescriptorFactory.fromResource(image(place.keyword)))
            mGoogleMap.addMarker(markerOptions)
        }
    }
    fun image(keyword:String):Int{
        var resultkeyword = 0
        if(keyword=="병원"){
            resultkeyword = R.drawable.ic_marker_shop
        }
        else if(keyword =="애견샾"){
            resultkeyword = R.drawable.ic_marker_hospital
        }
        return resultkeyword
    }
}

/*
1. 있던 예제를 이식해서 경로가 찍히고, 그 경로를 저장하는것 까지는 함
2. 해야될 것 2가지
2-1. 내가 저장한 경로 불러와서 지도와 함께 다른 화면에 표시하기 - 우선 홈 프래그먼트에 지도 2개 만들어서, 버튼 하나 만들어서 해보기
화면 캡처 / 지도에 경로 저장 2가지 경우가 다 있음.
펫피(네이버) : 화면 캡처 / 바라봄(구글) : 지도에 경로가 저장됨 / 마베독(카카오) : 지도에 경로가 저장됨.
맵은 하나만 불러와서 그걸 재활용할지, 아니면 맵을 글쓰기나 나의 활동관리 목록에서 또 만들어야할지.
그리고, 이러한 지도에 대해서, 구글 맵 / 네이버지도 / 카카오맵 등등이 있는데, 어떤 어플은 어떤걸 썻는지도 확인하면 좋음.
왜냐면 , 데이터 받아오는 비용이나 혹은 경로 저장같은 기능 등등 우리가 구현하고자 하는것들의 구현 난이도가 어떤게 좋을지 모르니까, 대체로 많이쓴것을
쓰면 좋음. 근데 일단은 3개다 제각각으로 쓰였음

mGoogleMap 자체를 저장하는것은 GoogleMap 객체는 많은 내부 상태와 연관된 UI 컴포넌트를 가지고 있어서 추천되지 않고,
현재 위치와 카메라, 줌
val cameraPosition = mGoogleMap.cameraPosition
val currentLatLng = cameraPosition.target
val currentZoom = cameraPosition.zoom
움직였던 경로
private var pathPoints = mutableListOf<LatLng>() // 사용자의 이동 경로 저장
private lateinit var path: Polyline // Polyline 객체

pathPoints.add(newLatLng) // 위치 데이터를 리스트에 저장
path.points = pathPoints // Polyline 업데이트
등 필요한 데이터만 저장하면 됨.

경로를 저장해서, 다른 mGoogleMap에 표현해보자.

다른 프래그먼트에서도, 구글맵을 표현해야 하기 때문에, 코드를 분리할 필요가 있다. 코드 정리하기

2-2. 애견샾, 동물병원 등등 API 받아와서 지도에 표시하기

2-3.
 */

/*
인풋 데이터와 아웃풋 데이터 함수는 호출해서 되는거 같지만, 현재 위치 고정이라던지 그런거 푸는것도 넣어야할듯.
그리고 아웃풋 데이터로 저장된 이동경로를 불러와도, 다시 이전의 경로로 바로 되돌아가는듯.
현재 데이터를 초기화하면서, 이후 아웃풋 데이터를 해야될거같음.
그리고 카메라 설정도 풀어야함.
함수를 프래그먼트쪽에서 카메라 설정 초기화하고 경로 초기화하고 그렇게 구글맵 초기화 후 아웃풋 데이타를 가저오는식으로.

아니면, 어짜피 게시글 쪽에서 지도 띄워야하는데, 그때 또 구글맵 만들텐데
그 때 구글맵 불러오고, 데이터 정의해주면 될듯. 그 땐 현재위치 그런거 없애고.

있어야 될꺼 :
홈은 데이터 저장 & 테스트 프래그먼트 이동
테스트는 데이터 불러오기 & 홈 프래그로 이동

 스크롤 리스너
 구글 지도에서
 스크롤 하는걸 감지를 하니까, 스크롤 할때는 버튼 hide 하면 좋을듯(플로팅버튼)
 */