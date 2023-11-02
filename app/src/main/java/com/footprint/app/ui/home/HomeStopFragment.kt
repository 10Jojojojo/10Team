package com.footprint.app.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.footprint.app.R
import com.footprint.app.databinding.FragmentHomeStopBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


class HomeStopFragment : Fragment(R.layout.fragment_home_stop) {
    private var _binding: FragmentHomeStopBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeStopBinding.bind(view)
        initView()
        initGoogleMap()
    }
    private fun initView() {
        binding.tvWalkdistancevalue.text =
            homeViewModel.walkList[homeViewModel.walkList.size - 1].distance
        binding.tvWalktimevalue.text =
            homeViewModel.walkList[homeViewModel.walkList.size - 1].walktime
        binding.tvWalkstarttimevalue.text =
            homeViewModel.walkList[homeViewModel.walkList.size - 1].starttime
        binding.tvWalkendtimevalue.text =
            homeViewModel.walkList[homeViewModel.walkList.size - 1].endtime
        binding.tvWalkdatevalue.text =
            homeViewModel.walkList[homeViewModel.walkList.size - 1].date
    }
    private fun initGoogleMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragmentstop) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            val lastWalk = homeViewModel.walkList[homeViewModel.walkList.size - 1].currentLocation
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    homeViewModel.walkList[homeViewModel.walkList.size - 1].pathpoint[0][0],
                    20f
                )
            )
            val cameraPosition = CameraPosition.Builder()
                .target(lastWalk.target)  // 카메라 의 타겟 위치
                .zoom(lastWalk.zoom)
                .tilt(lastWalk.tilt) // 카메라 의 회전 상태
                .build()

            // 카메라 를 해당 위치로 이동
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            addMarker(googleMap, R.drawable.ic_pawprint_on,homeViewModel.walkList[homeViewModel.walkList.size - 1].pathpoint.first().first() )
            addMarker(googleMap, R.drawable.ic_pawprint_off, homeViewModel.walkList[homeViewModel.walkList.size - 1].pathpoint.last().last())
            for (path in homeViewModel.walkList[homeViewModel.walkList.size - 1].pathpoint) {
                googleMap.addPolyline(PolylineOptions().addAll(path).color(Color.parseColor("#${homeViewModel.colorCode}")).width(homeViewModel.lineWidthText.toFloat()))
            }
        }
    }
    private fun addMarker(googleMap: GoogleMap, drawableResId: Int, position: LatLng) {
        val bitmap = BitmapFactory.decodeResource(resources, drawableResId)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
        val customMarker = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

        val markerOptions = MarkerOptions()
            .position(position)
            .icon(customMarker)

        googleMap.addMarker(markerOptions)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}