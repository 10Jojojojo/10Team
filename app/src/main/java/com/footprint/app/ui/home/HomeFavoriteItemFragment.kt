package com.footprint.app.ui.home

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.footprint.app.R
import com.footprint.app.databinding.FragmentHomeFavoriteItemBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.PolylineOptions

class HomeFavoriteItemFragment : Fragment(R.layout.fragment_home_favorite_item) {
    private var _binding: FragmentHomeFavoriteItemBinding? = null
    private val binding get() = _binding!!
    private val index = arguments?.getInt("position",0)!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeFavoriteItemBinding.bind(view)
        initView()
        initGoogleMap()
    }
    private fun initView() {
        binding.tvWalkdistancevalue.text =
            homeViewModel.walkList[index].distance
        binding.tvWalktimevalue.text =
            homeViewModel.walkList[index].walktime
        binding.tvWalkstarttimevalue.text =
            homeViewModel.walkList[index].starttime
        binding.tvWalkendtimevalue.text =
            homeViewModel.walkList[index].endtime
        binding.tvWalkdatevalue.text =
            homeViewModel.walkList[index].date
    }
    private fun initGoogleMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragmentstop) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            val lastWalk = homeViewModel.walkList[index].currentLocation
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    homeViewModel.walkList[index].pathpoint[0][0],
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

            for (path in homeViewModel.walkList[index].pathpoint) {
                googleMap.addPolyline(PolylineOptions().addAll(path).color(Color.BLUE))
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
