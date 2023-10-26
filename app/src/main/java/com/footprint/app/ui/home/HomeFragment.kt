package com.footprint.app.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.footprint.app.R
import com.footprint.app.api.model.FlagModel
import com.footprint.app.api.model.WalkModel
import com.footprint.app.databinding.DialogHomeFlagBinding
import com.footprint.app.databinding.DialogHomeWalkBinding
import com.footprint.app.databinding.DialogHomeWalkstopBinding
import com.footprint.app.databinding.FragmentHomeBinding
import com.footprint.app.services.MyService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

//    //산책 시작,종료 마커는 지워지지 않게 하기위한 변수
//    private var startMarker: Marker? = null
//    private var endMarker: Marker? = null

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
            if ((homeViewModel.pathPoints.value!![homeViewModel.pathPoints.value!!.size - 1].size >= 2)) {
                val distanceInKm = homeViewModel.getDistance() / 1000.0
                binding.tvWalkdistancevalue.text = String.format("%.2fkm", distanceInKm)
            } else if (homeViewModel.pathPoints.value?.firstOrNull()?.isEmpty() == true) {
                binding.tvWalkdistancevalue.text = String.format("%.2fkm", 0.00)
            }
        }
        homeViewModel.walkstate.observe(viewLifecycleOwner) {
            updateWalkStateUI()
        }
    }

    private fun updateWalkStateUI() {
        when (homeViewModel.walkstate.value) {
            "산책중" -> {
                binding.ivPause.setImageResource(R.drawable.ic_pause)
                binding.ivPawprint.alpha = 1f
                binding.ivPawprint.setImageResource(R.drawable.ic_pawprint_on)
                binding.ivPawprint.tag = R.drawable.ic_pawprint_on
            }

            "산책일시정지" -> {
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
//        binding.testinputbutton.setOnClickListener {
////            homeViewModel.inputdata(mGoogleMap, homeViewModel.pathPoints.value!!, path)
//
//        }
//        binding.testoutputbutton.setOnClickListener {
////            homeViewModel.outputdata(mGoogleMap)
//            placeMarkersOnMap(homeViewModel.placeitems)
//        }
//        binding.testapibutton.setOnClickListener {
//            homeViewModel.getPlaces(null, "병원", "")
//            homeViewModel.getPlaces(null, "애견샾", "")
//        }
//        binding.testpathlogbutton.setOnClickListener {
////            Log.d("GoogleMapPractice", "저장된 경로 : ${homeViewModel.pathPoints.value!!}")
//        }
        binding.ivPause.setOnClickListener {
            // 일시정지 기능
            homeViewModel.pauseWalk()
        }
        binding.ivPawprint.setOnClickListener {
            // 산책시작 기능
            showDialogWalkstart()
        }
        binding.ivSquare.setOnClickListener {
            // 산책정지 기능
            if (homeViewModel.walkstate.value != "산책종료") {
//                addMarker(R.drawable.ic_pawprint_off)
                showDialogWalkstate()
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
    }

    // 지도 프래그먼트를 가져와서 OnMapReadyCallback을 등록

    // 지도가 준비되었을 때 호출되는 콜백
    // 입력값은 childFragmentManager에서 입력하는 R.id.map_fragment값이 된다.
    override fun onMapReady(p0: GoogleMap) {
        mGoogleMap = p0
        mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        // 위치 정보를 받음 Activity가 아닌 Fragment라 this를 requireContext()로 수정
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Polyline 초기 설정
        polyline = mGoogleMap.addPolyline(
            PolylineOptions().color(Color.RED).width(10f)
//                .pattern(listOf(Dot(), Gap(10f), Dash(30f), Gap(10f)))
//                    .startCap(SquareCap())
//                    .endCap(SquareCap())
        )

        //지도 클릭 리스너
        mGoogleMap.setOnMapClickListener { latLng ->
            if (markerstate) {
                showDialogFlag(latLng)
            }
        }
        //마커 클릭 리스너
        mGoogleMap.setOnMarkerClickListener { marker ->
            if (markerdelandinfo == "삭제") {

                val targetFlag = homeViewModel.flagList.find { it.latlng == marker.position }
                homeViewModel.flagList.remove(targetFlag)

                // 마커 삭제(시작마커,종료마커는 제외)
//                if (marker != startMarker && marker != endMarker) {
                marker.remove()
//                }

                // true 반환하여 기본 마커 클릭 이벤트를 방지
                true
            } else {
                // 정보 보기 모드일 때의 동작 (예: 마커의 정보창 표시)
                // false 반환하여 기본 마커 클릭 이벤트가 발생하도록
                false
            }
        }
        updateLocationUI()
    }

    private fun showDialogWalkstart() {
        val builder = AlertDialog.Builder(requireContext())
        val bindingDialog = DialogHomeWalkBinding.inflate(layoutInflater)
        builder.setView(bindingDialog.root)
        val dialog = builder.show()
        bindingDialog.btYes.setOnClickListener {
            dialog.dismiss()
            if (homeViewModel.walkstate.value!! == "산책종료") {
                starttime = SimpleDateFormat("a HH : mm", Locale.KOREA).format(Date())
                startLocationService()
//                addMarker(R.drawable.ic_pawprint_on)
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
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bindingDialog.btYes.setOnClickListener {
            dialog.dismiss()
            homeViewModel.walkList.add(
                WalkModel(
                    binding.tvWalkdistancevalue.text.toString(),
                    binding.tvWalktimevalue.text.toString(),
                    homeViewModel.pathPoints.value!!,
                    mGoogleMap.cameraPosition,
                    starttime = starttime,
                    endtime = SimpleDateFormat("a HH : mm", Locale.KOREA).format(Date())
                )
            )
            captureMapSnapshot(mGoogleMap)
//            savedFilePath.observe(viewLifecycleOwner){
//            destroyPolyline()
//            stopLocationService()
//            binding.ivPawprint.setImageResource(R.drawable.ic_pawprint_off)
//            findNavController().navigate(R.id.homestop)
//            homeViewModel.endWalk()
//            }
        }
        bindingDialog.btNo.setOnClickListener {
            dialog.dismiss()
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
            dialog.dismiss()
            // 커스텀 아이콘으로 비트맵을 가져옴
            val originalBitmap = BitmapFactory.decodeResource(resources, flag)

            // 원하는 크기로 비트맵 크기를 조절
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 80, 80, false)

            // 조절된 비트맵으로 아이콘을 설정
            val customIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

            // 클릭한 위치에 커스텀 아이콘을 사용하여 마커를 추가
            val marker = mGoogleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(text)
                    .icon(customIcon)
            )
            homeViewModel.flagList.add(FlagModel(R.drawable.ic_flag, text, latLng, marker))
            Log.d("FootprintApp", "${homeViewModel.flagList}")
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
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }

        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun setLastLocation(location: Location) {
        val newLatLng = LatLng(location.latitude, location.longitude)
        if (homeViewModel.walkstate.value == "산책중") {
            homeViewModel.pathPoints.value?.let { pathPoints ->

                // 현재 위치를 가장 마지막 경로 리스트에 추가
                pathPoints[pathPoints.size - 1].add(newLatLng)

                // 모든 폴리라인을 지도에서 제거
                polylineList.forEach { it.remove() }
                polylineList.clear()

                // 모든 폴리라인을 다시 지도에 그림
                pathPoints.forEach { path ->
                    val polylineOptions = PolylineOptions().addAll(path)
                        .color(Color.RED).width(10f)
//                        .pattern(listOf(Dot(), Gap(10f), Dash(30f), Gap(10f)))
                    val newPolyline = mGoogleMap.addPolyline(polylineOptions)
                    polylineList.add(newPolyline)
                }
            }
        }
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng)) // 카메라를 현재 위치로 이동
    }

//    private fun placeMarkersOnMap(places: List<PlaceModel>) {
//        for (place in places) {
//            val location = LatLng(place.location.lat, place.location.lng)
//            val markerOptions = MarkerOptions()
//                .position(location)
////                .title(place.name)
////                .snippet(place.address)
//                .icon(BitmapDescriptorFactory.fromResource(image(place.keyword)))
//            mGoogleMap.addMarker(markerOptions)
//        }
//    }

//    private fun image(keyword: String): Int {
//        var resultkeyword = 0
//        if (keyword == "병원") {
//            resultkeyword = R.drawable.ic_marker_shop
//        } else if (keyword == "애견샾") {
//            resultkeyword = R.drawable.ic_marker_hospital
//        }
//        return resultkeyword
//    }

    private fun captureMapSnapshot(googleMap: GoogleMap) {
        // 코루틴 스코프 내에서 비동기 작업 시작
        CoroutineScope(Dispatchers.Main).launch {
            val snapshotPath = saveMapSnapshot(googleMap)
            homeViewModel.walkList[homeViewModel.walkList.size - 1].snapshotPath = snapshotPath

            destroyPolyline()
            stopLocationService()
            binding.ivPawprint.setImageResource(R.drawable.ic_pawprint_off)
            findNavController().navigate(R.id.homestop)
            homeViewModel.endWalk()
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

    //    private fun addMarker(drawable: Int) {
//        val bitmap = BitmapFactory.decodeResource(resources, drawable)
//        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
//        val customMarker = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
//
//        val markerOptions = MarkerOptions()
//            .position(mGoogleMap.cameraPosition.target)
//            .icon(customMarker)
//        // 특별한 마커를 변수에 저장
////        if(startMarker == null){
////            startMarker = mGoogleMap.addMarker(markerOptions)
////        } else if(endMarker == null){
////            endMarker = mGoogleMap.addMarker(markerOptions)
////        }
//        mGoogleMap.addMarker(markerOptions)
//    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationReceiver)
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

 //        googleMap.snapshot { bitmap ->
//            // 외부 파일 저장 디렉토리에 "map_snapshot[index].png" 파일을 생성
//            val file = File(context?.getExternalFilesDir(null), "map_snapshot["+homeViewModel.walkList[homeViewModel.walkList.size-1].toString() +"].png")
//            val fos = FileOutputStream(file)
//            // 비트맵을 PNG 형식으로 파일에 저장
//            bitmap?.compress(Bitmap.CompressFormat.PNG, 90, fos)
//            fos.close()
//            // 파일의 절대 경로를 deferredPath에 설정
//            deferredPath.complete(file.absolutePath)
//        }
        // 위의 코드가 문제가 있다는데 먼지 잘 모르겠음. 나중에 알아보기.
        googleMap.snapshot { bitmap ->
            CoroutineScope(Dispatchers.Main).launch {
                val path = withContext(Dispatchers.IO) {
                    // 외부 파일 저장 디렉토리에 "map_snapshot[index].png" 파일을 생성
                    val file = File(context?.getExternalFilesDir(null), "map_snapshot[" + homeViewModel.walkList[homeViewModel.walkList.size-1].dateid + "].png")
                    val fos = FileOutputStream(file)
                    // 비트맵을 PNG 형식으로 파일에 저장
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 90, fos)
                    fos.close()

                    file.absolutePath // 이 값을 반환합니다.
                }
                deferredPath.complete(path)
            }
        }
 */