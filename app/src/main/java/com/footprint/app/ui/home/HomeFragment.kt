package com.footprint.app.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
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
import com.footprint.app.GoogleMapUtil
import com.footprint.app.R
import com.footprint.app.databinding.FragmentHomeBinding
import com.footprint.app.services.MyService
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// Fragment()에 R.layout.fragment_home를 입력으로 주면, 생성자 주입이라고 하는건데 (공식문서에는없음)
// onCreateView에서 하는동작을 대체해줌.
class HomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback {
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
    private var dialogManager: HomeDialogManager? = null

    // 현재 위치로 돌아가는 작업을 관리하는 Job
    private var returnToLocationJob: Job? = null

    // 사용자의 스크롤 여부를 확인하는 플래그
    private var isUserInteracting = false

    // ViewModel 인스턴스 생성
    // 생성자에 입력값이 생기면 다른 방식으로 생성해주어야 하고, ViewModelFactory도 변경해주어야 한다.
    // private val homeViewModel by lazy { ViewModelProvider(this)[HomeViewModel::class.java] }
    // 다른 프래그먼트에서도 homeViewModel 인스턴스를 참조하기 위해 수정
    private val homeViewModel by activityViewModels<HomeViewModel>()

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
        // 산책하는 시간의 변화가 감지되면 산책 시간을 표기하고, 거리를 측정하여 표기한다
        homeViewModel.time.observe(viewLifecycleOwner) {
            binding.tvWalktimevalue.text = it
            homeViewModel.pathPoints.value?.let { pathPoints ->
                if ((pathPoints[pathPoints.size - 1].size >= 2)) {
                    val distanceInKm = homeViewModel.getDistance() / 1000.0
                    binding.tvWalkdistancevalue.text = String.format("%.2fkm", distanceInKm)
                } else if (pathPoints.first().isEmpty()) {
                    binding.tvWalkdistancevalue.text = String.format("%.2fkm", 0.00)
                }
            }
        }
        homeViewModel.walkState.observe(viewLifecycleOwner) {
            updateWalkStateUI()
        }
    }

    private fun updateWalkStateUI() {
        when (homeViewModel.walkState.value) {
            "산책중" -> {
                binding.ivPause.setImageResource(R.drawable.ic_pause)
                binding.ivPawprint.alpha = 1f
                binding.ivPawprint.setImageResource(R.drawable.ic_pawprint_on)
                binding.ivPawprint.tag = R.drawable.ic_pawprint_on
            }

            "산책 일시 정지" -> {
                binding.ivPause.setImageResource(R.drawable.ic_play)
                binding.ivPawprint.alpha = 0.3f
            }

            "산책종료" -> {
                binding.ivPawprint.setImageResource(R.drawable.ic_pawprint_off)
                binding.ivPause.setImageResource(R.drawable.ic_pause)
                binding.ivPawprint.alpha = 1f
            }
        }
    }

    private fun initView() { // private는 여기 이 프래그먼트에서만 쓸꺼라는거
        binding.ivPause.setOnClickListener {
            // 일시정지 기능
            homeViewModel.pauseWalk()
            when (homeViewModel.walkState.value) {
                "산책 일시 정지" -> {
                    pauseMarkerList.add(
                        GoogleMapUtil.addMarker(
                            requireContext(),
                            mGoogleMap,
                            currentLatLng,
                            R.drawable.ic_placeholder_pause,
                            "일시정지"
                        )
                    )
                }

                "산책중" -> {
                    restartMarkerList.add(
                        GoogleMapUtil.addMarker(
                            requireContext(),
                            mGoogleMap,
                            currentLatLng,
                            R.drawable.ic_placeholder_restart,
                            "산책중"
                        )
                    )
                }
            }
        }
        binding.ivPawprint.setOnClickListener {
            // 산책시작 기능
            dialogManager?.showDialogWalkstart(currentLatLng)
        }
        binding.ivSquare.setOnClickListener {
            // 산책정지 기능
            if (homeViewModel.walkState.value != "산책종료") {
                dialogManager?.showDialogWalkstate(currentLatLng,starttime)
            }

        }
        binding.ivFlag.setOnClickListener {
            if (markerstate) {
                binding.ivFlag.alpha = 0.3f
            } else {
                binding.ivFlag.alpha = 1f
            }
            markerstate = !markerstate
        }
        binding.ivFavorite.setOnClickListener {
            findNavController().navigate(R.id.homefavorite)
        }
        binding.ivFlaginfoanddel.setOnClickListener {
            when (markerdelandinfo) {
                "정보" -> {
                    markerdelandinfo = "삭제"
                    binding.ivFlaginfoanddel.setImageResource(R.drawable.ic_flagdelete)
                }

                "삭제" -> {
                    markerdelandinfo = "정보"
                    binding.ivFlaginfoanddel.setImageResource(R.drawable.ic_flaginfo)
                }
            }
        }
        binding.vExampleline.setOnClickListener {
            dialogManager?.showDialogPolyline()
        }
    }

// 지도 프래그먼트를 가져와서 OnMapReadyCallback을 등록

    // 지도가 준비되었을 때 호출되는 콜백
// 입력값은 childFragmentManager에서 입력하는 R.id.map_fragment값이 된다.
    override fun onMapReady(p0: GoogleMap) {
        mGoogleMap = p0

        // 다이어로그 매니저에 구글맵 변수가 들어가므로, 초기화 이후 함수 호출
        createDialogManager()

        mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        // 위치 정보를 받음 Activity가 아닌 Fragment라 this를 requireContext()로 수정
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Polyline 초기 설정
        polyline = mGoogleMap.addPolyline(
            PolylineOptions().color(Color.parseColor("#${homeViewModel.colorCode}"))
                .width(homeViewModel.lineWidthText.toFloat())
//                .pattern(listOf(Dot(), Gap(10f), Dash(30f), Gap(10f)))
//                    .startCap(SquareCap())
//                    .endCap(SquareCap())
        )

        //지도 클릭 리스너
        mGoogleMap.setOnMapClickListener { latLng ->
            if (markerstate) {
                dialogManager?.showDialogFlag(latLng)
            }
        }
        //마커 클릭 리스너
        mGoogleMap.setOnMarkerClickListener { marker ->
            if (markerdelandinfo == "삭제") {

                val targetFlag = homeViewModel.flagList.find { it.latlng == marker.position }
                homeViewModel.flagList.remove(targetFlag)

                // 마커 삭제(시작마커,종료마커는 제외)
                if (marker != startMarker
                    && marker != currentMarker
                    && marker != endMarker
                    && !restartMarkerList.contains(marker)
                    && !pauseMarkerList.contains(marker)
                ) {
                    marker.remove()
                }
                // true 반환하여 기본 마커 클릭 이벤트를 방지
                true
            } else {
                // 정보 보기 모드일 때의 동작 (예: 마커의 정보창 표시)
                // false 반환하여 기본 마커 클릭 이벤트가 발생하도록
                false
            }
        }
        mGoogleMap.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                // 사용자가 지도를 움직였을 때
                isUserInteracting = true
                // 이전에 예약된 작업이 있다면 취소합니다.
                returnToLocationJob?.cancel()
            }
        }

        mGoogleMap.setOnCameraIdleListener {
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
            currentMarker = GoogleMapUtil.addMarkerAndText(
                requireContext(),
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
    private suspend fun saveMapSnapshot(googleMap: GoogleMap): String {
        // 스냅샷 경로를 저장할 변수를 비동기적으로 초기화
        val deferredPath = CompletableDeferred<String>()
        googleMap.snapshot { bitmap ->
            CoroutineScope(Dispatchers.Main).launch {
                val path = withContext(Dispatchers.IO) {
                    // 외부 파일 저장 디렉토리에 "map_snapshot[index].png" 파일을 생성
                    val file = File(
                        context?.getExternalFilesDir(null),
                        "map_snapshot[" + homeViewModel.walkList[homeViewModel.walkList.size - 1].dateid + "].png"
                    )
                    val fos = FileOutputStream(file)
                    // 비트맵을 PNG 형식으로 파일에 저장
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 90, fos)
                    fos.close()

                    file.absolutePath // 이 값을 반환합니다.
                }
                deferredPath.complete(path)
            }
        }
        // 스냅샷의 파일 경로를 반환
        // await()를 통해 해당 경로가 설정될 때까지 기다린다.
        return deferredPath.await()
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
    private fun createDialogManager() {
        dialogManager = HomeDialogManager(
            requireContext(),
            homeViewModel,
            mGoogleMap,
            layoutInflater,
            binding,
            viewLifecycleOwner,
            startLocationService = {
                // 위치 서비스 시작 로직 구현
                startLocationService()
            },
            captureMapSnapshot = { googleMap ->
                // 코루틴 스코프 내에서 비동기 작업 시작
                CoroutineScope(Dispatchers.Main).launch {
                    val snapshotPath = saveMapSnapshot(googleMap)
                    homeViewModel.walkList[homeViewModel.walkList.size - 1].snapshotPath =
                        snapshotPath

                    destroyPolyline()
                    stopLocationService()
                    binding.ivPawprint.setImageResource(R.drawable.ic_pawprint_off)
                    findNavController().navigate(R.id.homestop)
                    homeViewModel.endWalk()
                    delStartEndMarker()
                }
            },
            onWalkStarted = { time, marker ->
                starttime = time
                startMarker = marker
            },
            onWalkEnded = {marker ->
                endMarker = marker
            },
            showToast = { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        returnToLocationJob?.cancel() // 프래그먼트/액티비티가 종료될 때 코루틴을 취소
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationReceiver)
    }
}