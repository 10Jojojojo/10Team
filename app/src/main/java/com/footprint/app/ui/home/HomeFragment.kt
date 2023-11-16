package com.footprint.app.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.footprint.app.Constants.BIND_OFF
import com.footprint.app.Constants.BIND_ON
import com.footprint.app.FirebaseDatabaseManager.deleteMarkerdata
import com.footprint.app.FirebaseDatabaseManager.uploadImage
import com.footprint.app.R
import com.footprint.app.addFlagsToMap
import com.footprint.app.addMarker
import com.footprint.app.addMarkerAndText
import com.footprint.app.api.model.PetInfoWalkModel
import com.footprint.app.api.model.WalkModel
import com.footprint.app.captureMapSnapshot
import com.footprint.app.databinding.DialogHomeFlagBinding
import com.footprint.app.databinding.DialogHomePolylineBinding
import com.footprint.app.databinding.DialogHomeWalkBinding
import com.footprint.app.databinding.DialogHomeWalkstopBinding
import com.footprint.app.databinding.FragmentHomeBinding
import com.footprint.app.formatTimeMinSec
import com.footprint.app.services.MyService
import com.footprint.app.ui.mypage.MyPageViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Fragment()에 R.layout.fragment_home를 입력으로 주면, 생성자 주입이라고 하는건데 (공식문서에는없음)
// onCreateView에서 하는동작을 대체해줌.
class HomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback, View.OnClickListener {
    private var _binding: FragmentHomeBinding? = null

    // GoogleMap 인스턴스를 참조하기 위한 변수
    // 화면 표시 요소로써, ViewModel이 아닌 Fragment에서 사용하고, Fragment에서만 사용하므로 private로 선언
    private lateinit var mGoogleMap: GoogleMap

    // 기기 위치 정보를 가져오는 여러 메서드를 제공하는 FusedLocationProviderClient 인스턴스를 참조하기 위한 변수
    // 화면 업데이트 요소로써, ViewModel이 아닌 Fragment에서 사용하고, Fragment에서만 사용하므로 private로 선언
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // 위치 권한을 얻기 위한 ActivityResultLauncher<Array<String>> 인스턴스를 참조하기 위한 변수
    // 화면 업데이트 요소의 권한을 요청하는 요소로써, ViewModel이 아닌 Fragment에서 사용하고, Fragment에서만 사용하므로 private로 선언
    private lateinit var locationPermission: ActivityResultLauncher<Array<String>>

    // 구글맵에 표시되는 선 객체
    // 화면 표시 요소로써, ViewModel이 아닌 Fragment에서 사용하고, Fragment에서만 사용하므로 private로 선언
    private lateinit var polyline: Polyline // Polyline 객체
    private val polylineList = mutableListOf<Polyline>()
    private val binding get() = _binding!!
    private var markerstate = false
    private var markerdelandinfo = "정보"
    private var starttime = ""
    private lateinit var currentLatLng: LatLng

    //산책 시작,종료 마커는 지워지지 않게 하기위한 변수
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var currentMarker: Marker? = null
    private var restartMarkerList: MutableList<Marker?> = mutableListOf()
    private var pauseMarkerList: MutableList<Marker?> = mutableListOf()

    // 현재 위치로 돌아가는 작업을 관리하는 Job
    private var returnToLocationJob: Job? = null

    // 사용자의 스크롤 여부를 확인하는 플래그
    private var isUserInteracting = false

    private lateinit var tempWalk:WalkModel
    private val markerList by lazy { homeViewModel.markerList.value ?: mutableListOf() }

    // ViewModel 인스턴스 생성
    // 생성자에 입력값이 생기면 다른 방식으로 생성해주어야 하고, ViewModelFactory도 변경해주어야 한다.
    // private val homeViewModel by lazy { ViewModelProvider(this)[HomeViewModel::class.java] }
    // 다른 프래그먼트에서도 homeViewModel 인스턴스를 참조하기 위해 수정
    private val homeViewModel by activityViewModels<HomeViewModel>()

    private val myPageViewModel by activityViewModels<MyPageViewModel>()
    private var petInfoWalks = mutableListOf<PetInfoWalkModel>()

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)
            // 여기에서 위도/경도를 처리
            val location = Location(LocationManager.GPS_PROVIDER).apply {
                this.latitude = latitude
                this.longitude = longitude
                currentLatLng = LatLng(latitude, longitude)
            }
            // 받아온 위치로 지도 UI를 업데이트
            setLastLocation(location)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        getPermission() // 권한을 먼저 설정하고, 맵을 불러오자.
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationReceiver,
            IntentFilter("LOCATION_UPDATE")
        )
        initView()
        getMap()
        observeViewModel() // 뷰를 먼저 생성하고 LiveData를 감시해야 한다.(LiveData의 변화가 먼저 일어나면,
        // 뷰는 생성되지 않았는데(초기화가 되지 않았는데) 뷰의 속성 변화를 하게 될 수 있다.)
    }

    private fun startLocationService() {
        Intent(requireContext(), MyService::class.java).also {
            requireContext().startService(it)
        }
    }

    private fun stopLocationService() {
        Intent(requireContext(), MyService::class.java).also {
            requireContext().stopService(it)
        }
    }

    private fun observeViewModel() {
        if (!homeViewModel.getHomeObserve()) {
            // 산책하는 시간의 변화가 감지되면 산책 시간을 표기하고, 거리를 측정하여 표기한다
            homeViewModel.time.observe(viewLifecycleOwner) {
                Log.d("aaaaaa123","${homeViewModel.walkState.value}")
                homeViewModel.getDistance()
                binding.tvWalktimevalue.text = formatTimeMinSec(it)
                homeViewModel.pathPoints.value?.let { pathPoints ->
                    if ((pathPoints[pathPoints.size - 1].size >= 2)) {
                        binding.tvWalkdistancevalue.text = String.format("%.2fkm", homeViewModel.distance / 1000.0)
                    } else if (pathPoints.first().isEmpty()) {
                        binding.tvWalkdistancevalue.text = String.format("%.2fkm", 0.00)
                    }
                }
            }
            homeViewModel.walkState.observe(viewLifecycleOwner) {
                updateWalkStateUI()
            }
//            homeViewModel.walkList.observe(viewLifecycleOwner) {
//            }
            homeViewModel.isMapReady.observe(viewLifecycleOwner) {
                // 구글맵이 준비되면 markerList도 관찰을 시작한다.
                // 이유는 구글맵에 마커를 추가하는 작업이 있는데, 구글맵이 준비되기 전에 작업하면 안되기 때문
                // 한번 isMapReady가 바뀌어서 하단의 코드가 실행되면, 이후로 UI 컴포넌트의 생명주기가 끝날때까지 계속 관찰하게 된다.
                if (!homeViewModel.getHomeObserve2()) {
                    homeViewModel.markerList.observe(viewLifecycleOwner) {
                        for (i in 0..<markerList.size) {
                            if (markerList[i].bindState == BIND_OFF) {
                                    requireContext().addFlagsToMap(mGoogleMap,markerList[i], 80, 80)
                            }
                                markerList[i].bindState = BIND_ON
                            }
                    }
                    homeViewModel.setHomeObserve2(true)
                }
            }
            homeViewModel.setHomeObserve(true)
        }
    }

    private fun updateWalkStateUI() {
        binding.apply {
            when (homeViewModel.walkState.value) {
                "산책중" -> {
                    ivPause.setImageResource(R.drawable.ic_pause)
                    ivPawprint.alpha = 1f
                    ivPawprint.setImageResource(R.drawable.ic_pawprint_on)
                    ivPawprint.tag = R.drawable.ic_pawprint_on
                }

                "산책 일시 정지" -> {
                    ivPause.setImageResource(R.drawable.ic_play)
                    ivPawprint.alpha = 0.3f
                }

                "산책종료" -> {
                    ivPawprint.setImageResource(R.drawable.ic_pawprint_off)
                    ivPause.setImageResource(R.drawable.ic_pause)
                    ivPawprint.alpha = 1f
                }
            }
        }
    }


    private fun initView() { // private는 여기 이 프래그먼트에서만 쓸꺼라는거
        binding.ivPause.setOnClickListener(this) // 코드의 가독성을 높이기 위해 각각의 setOnClickListner는 override fun onClick(view: View?) 에서 구현.
        binding.ivPawprint.setOnClickListener(this)
        binding.ivSquare.setOnClickListener(this)
        binding.ivFlag.setOnClickListener(this)
        binding.ivFavorite.setOnClickListener(this)
        binding.ivFlaginfoanddel.setOnClickListener(this)
//        binding.vExampleline.setOnClickListener(this)
    }

// 지도 프래그먼트를 가져와서 OnMapReadyCallback을 등록

    // 지도가 준비되었을 때 호출되는 콜백
// 입력값은 childFragmentManager에서 입력하는 R.id.map_fragment값이 된다.
    override fun onMapReady(p0: GoogleMap) {

        // 위치 정보를 받음 Activity가 아닌 Fragment라 this를 requireContext()로 수정
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        mGoogleMap = p0
        homeViewModel.setMapReady()
        mGoogleMap.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL

            // Polyline 초기 설정
            polyline = addPolyline(
                PolylineOptions().color(Color.parseColor("#${homeViewModel.colorCode}"))
                    .width(homeViewModel.lineWidthText.toFloat())
//                .pattern(listOf(Dot(), Gap(10f), Dash(30f), Gap(10f)))
//                    .startCap(SquareCap())
//                    .endCap(SquareCap())
            )

            //지도 클릭 리스너
            setOnMapClickListener { latLng ->
                if (markerstate) {
                    showDialogFlag(latLng)
                }
            }
            //마커 클릭 리스너
            setOnMarkerClickListener { marker ->
                    if (markerdelandinfo == "삭제") {
                        // 마커 삭제(시작마커,종료마커는 제외)
                        if (marker != startMarker
                            && marker != currentMarker
                            && marker != endMarker
                            && !restartMarkerList.contains(marker)
                            && !pauseMarkerList.contains(marker)
                        ) {
                        // ViewModel에서 해당 마커에 해당하는 MarkerModel 객체를 찾기
//                        val markerModel =
//                            homeViewModel.markerList.value?.find { it.marker?.equals(marker) == true }

                        val targetFlag =
                            homeViewModel.markerList.value?.find { it.latlng == marker.position }
//                            // ViewModel 리스트에서도 마커 모델 제거
//                            homeViewModel.markerList.value?.remove(targetFlag)
                            // View에서 마커 제거
//                            marker.remove()
                        if (targetFlag?.markerKey != null) {
                            // Firebase에서 마커 삭제
                            deleteMarkerdata(targetFlag.markerKey!!) { success ->
                                if (success) {

                                    // ViewModel 리스트에서도 마커 모델 제거
                                    homeViewModel.markerList.value?.remove(targetFlag)
                                    // View에서 마커 제거
                                    marker.remove()
                                } else {
                                    // 실패 처리, 예를 들면 사용자에게 메시지를 표시
                                }
                            }
                        } else {
                            // 키가 없거나 마커 모델을 찾을 수 없는 경우의 처리
                        }
                    }
                    // true 반환하여 기본 마커 클릭 이벤트를 방지
                    true
                } else {
                    // 정보 보기 모드일 때의 동작 (예: 마커의 정보창 표시)
                    // false 반환하여 기본 마커 클릭 이벤트가 발생하도록
                    false
                }
            }
            setOnCameraMoveStartedListener { reason ->
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    // 사용자가 지도를 움직였을 때
                    isUserInteracting = true
                    // 이전에 예약된 작업이 있다면 취소합니다.
                    returnToLocationJob?.cancel()
                }
            }

            setOnCameraIdleListener {
                // 사용자가 지도 움직임을 멈추면
                if (isUserInteracting) {
                    // 3초 후에 현재 위치로 돌아가는 작업을 예약
                    returnToLocationJob = lifecycleScope.launch {
                        delay(3000) // 3초 대기
                        if (isUserInteracting) {
                            // 사용자가 다시 지도를 움직이지 않았다면 현재 위치로 이동
//                        currentLatLng.let {
//                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(it))
//                        }
                            // 사용자가 다시 지도를 움직이지 않았다면 현재 위치로 이동
                            currentLatLng.let { latLng ->
                                mGoogleMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        latLng,
                                        mGoogleMap.cameraPosition.zoom
                                    )
                                )
                            }
                            // 작업이 완료된 후에는 플래그를 다시 false로 설정
                            isUserInteracting = false
                        }
                    }
                }
            }
        }
        updateLocationUI()
    }

    private fun destroyPolyline() {
        for (polyline in polylineList) {
            polyline.remove()
        }
        polylineList.clear()
    }

    private fun getPermission() {
        // 이 아래의 21의 줄이 있어야 onMapReady가 콜백이 되고, updateLocationUI()가 동작하는것 같음.
        locationPermission = // 2. Permissions()를 검사한 후, results를 입력으로 주고, 그 결과가 만약 권한이 있다면,
                // .xml의 Fragment에 this를 연결시킴
                // 그 this는 GoogleMap
                // OnMapReadyCallback을 상속받는데, 이게 추가되면 override 메서드로 onMapReady가 필수로 들어가야함
                // onMapReadyCallback에 의해서 Map이 준비가되면 자동으로 onMapReady가 콜백이 됨.
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
                if (results.all { it.value }) {
                    (childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment).getMapAsync(
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

    private fun getMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

    }

    // 위치 관련 UI 업데이트 메서드
    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        try {
            Log.d("FootprintApp", "updataLocationUI")

            // 최초에 현재위치 받아와서 줌
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLatLng = LatLng(location.latitude, location.longitude)
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }

        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun setLastLocation(location: Location) {
        val newLatLng = LatLng(location.latitude, location.longitude)
        if (homeViewModel.walkState.value == "산책중") {
            homeViewModel.pathPoints.value?.let { pathPoints ->

                // 현재 위치를 가장 마지막 경로 리스트에 추가
                pathPoints[pathPoints.size - 1].add(newLatLng)

                // 모든 폴리라인을 지도에서 제거
                polylineList.forEach { it.remove() }
                polylineList.clear()

                // 모든 폴리라인을 다시 지도에 그림
                pathPoints.forEach { path ->
                    val polylineOptions = PolylineOptions().addAll(path)
                        .color(Color.parseColor("#${homeViewModel.colorCode}"))
                        .width(homeViewModel.lineWidthText.toFloat())
//                        .pattern(listOf(Dot(), Gap(10f), Dash(30f), Gap(10f)))
                    val newPolyline = mGoogleMap.addPolyline(polylineOptions)
                    polylineList.add(newPolyline)
                }
            }
        }
        if (currentMarker == null) {
            // 처음 위치를 설정할 때 마커를 추가합니다.
            currentMarker = requireContext().addMarkerAndText(
                mGoogleMap,
                currentLatLng,
                R.drawable.ic_placeholder_current,
                "내 위치"
            )
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(newLatLng))
        } else {
            // 위치가 업데이트 되면 마커의 위치를 갱신합니다.
            currentMarker!!.position = newLatLng
            if (!isUserInteracting) {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(newLatLng))
            }
        }
    }

    private fun delStartEndMarker() {
        startMarker?.remove()
        startMarker = null
        endMarker?.remove()
        endMarker = null
        for (i in 0..<restartMarkerList.size) {
            restartMarkerList[i]?.remove()
            restartMarkerList[i] = null
        }
        for (i in 0..<pauseMarkerList.size) {
            pauseMarkerList[i]?.remove()
            pauseMarkerList[i] = null
        }
    }

    private fun showDialogWalkstart() {
        val builder = AlertDialog.Builder(requireContext())
        val bindingDialog = DialogHomeWalkBinding.inflate(layoutInflater)
        builder.setView(bindingDialog.root)
        val dialog = builder.show()
        bindingDialog.btYes.setOnClickListener {
            dialog.dismiss()
            if (homeViewModel.walkState.value!! == "산책종료") {
                starttime = SimpleDateFormat("a HH : mm", Locale.KOREA).format(Date())
                startLocationService()
                requireContext().addMarker(
                    mGoogleMap, currentLatLng,
                    R.drawable.ic_placeholder_start, "산책시작"
                ) {startMarker = it.marker}
            }
            homeViewModel.startWalk()
        }
        bindingDialog.btNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showDialogWalkstate() {

        val builder = AlertDialog.Builder(requireContext())
        val bindingDialog = DialogHomeWalkstopBinding.inflate(layoutInflater)
        builder.setView(bindingDialog.root)
        val dialog = builder.show()
        // 다이어로그의 사각형 모서리를 둥글게 만들기 위해 콘스트레인트레이아웃의 색깔을 투명으로 만들기 위한 코드
        Log.d("aaaaaa1231","${homeViewModel.petInfoList.value}")
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bindingDialog.btYes.setOnClickListener {
            dialog.dismiss()
            homeViewModel.petList = mutableListOf()
            homeViewModel.petInfoList.value?.forEach {
                if (it.activePet) {
                    homeViewModel.petList.add(it.timestamp)
                }
            }
            Log.d("aaaaaa1232","${homeViewModel.petList}")
            homeViewModel.petList.forEach { timestamp ->
                homeViewModel.petInfoList.value?.find { it.timestamp == timestamp }?.let { petInfo ->
                    petInfoWalks.add(PetInfoWalkModel(petInfo.petName, petInfo.petImageUrl))
                }
            }
            Log.d("aaaaaa1233","$petInfoWalks")
            tempWalk = WalkModel(
                petList =  petInfoWalks, // 펫키 나중에 펫정보관련 작업되면 수정
                distance = homeViewModel.distance, // tvWalkdistancevalue , tvWalktimevalue
                walktime = homeViewModel.endTime,
                pathpoint = homeViewModel.pathPoints.value!!,
                currentLocation = mGoogleMap.cameraPosition,
                snapshotPath = "",  // 스냅샷 파일의 경로 저장
                starttime = homeViewModel.startTime,//starttime.toLong(),
                endtime = System.currentTimeMillis()
            )
            petInfoWalks = mutableListOf()
            stopLocationService() // 얘랑(위치)
            homeViewModel.endWalk() // 얘를(시간 멈추는거) 위로올리자. 시간오래걸리는 이미지업로드 위로.
            requireContext().addMarker(
                mGoogleMap, currentLatLng,
                R.drawable.ic_placeholder_end, "산책종료"
            ) {endMarker = it.marker}
            captureMapSnapshot(mGoogleMap) {
                // 사진을 찍고 uri 데이터를 저장하는 작업은 비동기작업인데,
                // 해당 경로를 받고 파이어베이스에 업로드를 하기 위한 작업도 비동기 작업이므로, 콜백함수를 통해 스냅샷 데이터를 만들고 저장한 후
                // 파이어베이스에 데이터를 업로드한다.
                it?.let {
                    // 파이어베이스에 업로드한후 얻은 url을 포함해서 데이터모델에 저장한 후 데이터모델을 파이어베이스에 업로드하는 비동기 작업
                    uploadImage(it) { mapImageUrl ->
                        tempWalk.snapshotPath = mapImageUrl
                        homeViewModel.updateWalk(tempWalk)
                        destroyPolyline()
                        binding.ivPawprint.setImageResource(R.drawable.ic_pawprint_off)
                        findNavController().navigate(R.id.homestop) // 화면 넘어가는 경우엔 가장 아래에 위치시키는게 좋다.
                        // 안그러면 화면이 넘어가서 binding 객체가 null이 되서 null인셉션이 발생해서?
                        // 개발의 흐름 트랜드 : 상태패턴
                        delStartEndMarker()
                    }
                }
            }
        }
        bindingDialog.btNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showDialogPolyline() {
        val builder = AlertDialog.Builder(requireContext())
        val bindingDialog = DialogHomePolylineBinding.inflate(layoutInflater)
        builder.setView(bindingDialog.root)
        val dialog = builder.show()
        // 다이어로그의 사각형 모서리를 둥글게 만들기 위해 콘스트레인트레이아웃의 색깔을 투명으로 만들기 위한 코드
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        homeViewModel.colorCodeData.observe(viewLifecycleOwner) { text ->
            updateShowDialogPolyline(bindingDialog, text)
        }
        homeViewModel.lineWidthTextData.observe(viewLifecycleOwner) { number ->
            updateShowDialogPolyline(bindingDialog, number)
        }
        bindingDialog.etColortext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                homeViewModel.updateColorCode(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        bindingDialog.etLinewidthtext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                homeViewModel.updateWidth(s.toString().toFloatOrNull())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        bindingDialog.btYes.setOnClickListener {
            // 색깔 코드 유효성 검사
            val colorCode = bindingDialog.etColortext.text.toString().trim()
            val colorPattern = "^[0-9a-fA-F]{6}$".toRegex()
            if (!colorPattern.matches(colorCode)) {
                showToast("색깔 코드를 올바르게 입력해주세요. (예: FFFFFF)")
                return@setOnClickListener
            }
            // 궤도의 두께 유효성 검사
            val lineWidthText = bindingDialog.etLinewidthtext.text.toString().trim()
            val lineWidth = lineWidthText.toIntOrNull()
            if (lineWidth == null || lineWidth < 0 || lineWidth > 100) {
                showToast("두께는 0부터 100까지의 숫자로 입력해주세요.")
                return@setOnClickListener
            }
            homeViewModel.colorCode = colorCode
            homeViewModel.lineWidthText = lineWidth.toString()
//            binding.vExampleline.setBackgroundColor(Color.parseColor("#${homeViewModel.colorCode}"))
//            bindingDialog.vExampleline.layoutParams.width = lineWidth
            dialog.dismiss()
        }
        bindingDialog.btNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun updateShowDialogPolyline(inflate: DialogHomePolylineBinding, colorCode: String) {
        val colorPattern = "^[0-9a-fA-F]{6}$".toRegex()
        if (!colorPattern.matches(colorCode)) {
            return
        }
        inflate.vExampleline.setBackgroundColor(Color.parseColor("#${colorCode}"))
    }

    private fun updateShowDialogPolyline(inflate: DialogHomePolylineBinding, width: Float?) {
        width?.let {
            if (width > 0.0F && width <= 100F) {
                inflate.vExampleline.layoutParams.height =
                    (width
//                            * (requireContext().resources.displayMetrics.density)
                            )
                        .toInt()
            }
        }
    }

    private fun showDialogFlag(latLng: LatLng) {
        val builder = AlertDialog.Builder(requireContext())
        val bindingDialog = DialogHomeFlagBinding.inflate(layoutInflater)
        builder.setView(bindingDialog.root)
        val dialog = builder.show()
        // 다이어로그의 사각형 모서리를 둥글게 만들기 위해 콘스트레인트레이아웃의 색깔을 투명으로 만들기 위한 코드
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var flag = R.drawable.ic_flag
        bindingDialog.ivFlag1.setOnClickListener {
            updateFlagSelection(bindingDialog, R.drawable.ic_flag)
            flag = R.drawable.ic_flag
        }
        bindingDialog.ivFlag2.setOnClickListener {
            updateFlagSelection(bindingDialog, R.drawable.ic_flag_blue)
            flag = R.drawable.ic_flag_blue
        }
        bindingDialog.ivFlag3.setOnClickListener {
            updateFlagSelection(bindingDialog, R.drawable.ic_flag_green)
            flag = R.drawable.ic_flag_green
        }
        bindingDialog.ivFlag4.setOnClickListener {
            updateFlagSelection(bindingDialog, R.drawable.ic_flag_purple)
            flag = R.drawable.ic_flag_purple
        }
        bindingDialog.ivFlag5.setOnClickListener {
            updateFlagSelection(bindingDialog, R.drawable.ic_flag_yellow)
            flag = R.drawable.ic_flag_yellow
        }

        bindingDialog.btYes.setOnClickListener {
            val text = bindingDialog.tvDialogtext.text.toString()
            requireContext().addMarker(mGoogleMap, latLng, flag, text, 80, 80) {
                homeViewModel.updateMarker(it)
            }
            dialog.dismiss()
        }
        bindingDialog.btNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun hideAllSelectionIndicators(binding: DialogHomeFlagBinding) {
        binding.ivFlag1select.visibility = View.GONE
        binding.ivFlag2select.visibility = View.GONE
        binding.ivFlag3select.visibility = View.GONE
        binding.ivFlag4select.visibility = View.GONE
        binding.ivFlag5select.visibility = View.GONE
    }

    private fun updateFlagSelection(binding: DialogHomeFlagBinding, selectedFlag: Int) {
        hideAllSelectionIndicators(binding)

        when (selectedFlag) {
            R.drawable.ic_flag -> {
                binding.ivFlag1select.visibility = View.VISIBLE
            }

            R.drawable.ic_flag_blue -> {
                binding.ivFlag2select.visibility = View.VISIBLE
            }

            R.drawable.ic_flag_green -> {
                binding.ivFlag3select.visibility = View.VISIBLE
            }

            R.drawable.ic_flag_purple -> {
                binding.ivFlag4select.visibility = View.VISIBLE
            }

            R.drawable.ic_flag_yellow -> {
                binding.ivFlag5select.visibility = View.VISIBLE
            }
        }
    }

    private fun showToast(message: String) {
        // 토스트 메시지 표시
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeViewModel.setHomeObserve(false)
        homeViewModel.setHomeObserve2(false)
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        returnToLocationJob?.cancel() // 프래그먼트/액티비티가 종료될 때 코루틴을 취소
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationReceiver)
    }

    override fun onClick(view: View?) {
        // view가 null 일수도있기떄문에 ?.와 let 스코프함수 이용해서 작성 그리고 null인경우에 ?: 엘비스 연산자 시계방향으로 90도꺾으면 엘비스 머리스타일이랑 비슷해서 엘비스연산자임
        view?.let {View->
            when (View) {
                binding.ivPause -> {            // 일시정지 기능
                    homeViewModel.pauseWalk()
                    when (homeViewModel.walkState.value) {
                        "산책 일시 정지" -> {
                             // 678~686의 경우에도 함수로 따로 빼서 할 수 있음. 그렇게하면 전체 코드는 늘어나더라도, 함수 이름만 잘 작성해두면
                                // 다른사람이 볼 때 직관적으로 빠르게 이해할 수 있으니까. 가독성을 증가시키는 느낌으로.
                                requireContext().addMarker(
                                    mGoogleMap,
                                    currentLatLng,
                                    R.drawable.ic_placeholder_pause,
                                    "일시정지")
                                 {pauseMarkerList.add(it.marker)}
                        }

                        "산책중" -> {

                                requireContext().addMarker(
                                    mGoogleMap,
                                    currentLatLng,
                                    R.drawable.ic_placeholder_restart,
                                    "산책중")
                                 {restartMarkerList.add(it.marker)}
                        }

                        else -> {}
                    }
                }

                binding.ivPawprint -> { // 산책시작 기능
                    showDialogWalkstart()
                }

                binding.ivSquare -> {
                    // 산책정지 기능
                    if (homeViewModel.walkState.value != "산책종료") {
                        showDialogWalkstate()
                    } else {
                    }
                }

                binding.ivFlag -> {
                    if (markerstate) {
                        binding.ivFlag.alpha = 0.3f
                    } else {
                        binding.ivFlag.alpha = 1f
                    }
                    markerstate = !markerstate
                }

                binding.ivFavorite -> {
                    findNavController().navigate(R.id.homefavorite)
                }

                binding.ivFlaginfoanddel -> {
                    when (markerdelandinfo) {
                        "정보" -> {
                            markerdelandinfo = "삭제"
                            binding.ivFlaginfoanddel.setImageResource(R.drawable.ic_flagdelete)
                        }

                        "삭제" -> {
                            markerdelandinfo = "정보"
                            binding.ivFlaginfoanddel.setImageResource(R.drawable.ic_flaginfo)
                        }

                        else -> {}
                    }
                }

//                binding.vExampleline -> {
//                    showDialogPolyline()
//                }

                else -> {}
            }
        }
    }
}