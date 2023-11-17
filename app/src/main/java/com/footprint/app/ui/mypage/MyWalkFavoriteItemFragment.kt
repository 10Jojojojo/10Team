package com.footprint.app.ui.mypage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.footprint.app.R
import com.footprint.app.databinding.FragmentHomeFavoriteItemBinding
import com.footprint.app.formatDateYmd
import com.footprint.app.formatDistance
import com.footprint.app.formatTimeHourMin
import com.footprint.app.formatTimeMinSec
import com.footprint.app.ui.home.HomeViewModel
import com.footprint.app.ui.home.PetAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MyWalkFavoriteItemFragment : Fragment(R.layout.fragment_my_walk_favorite_item) {
    private var _binding: FragmentHomeFavoriteItemBinding? = null
    private val binding get() = _binding!!

    //arguments 에 대해, by lazy 없이 멤버 변수로 초기화 하면 이때는 아직 null 이 나온다. 그래서 지연 초기화 를 한다.
    private val index by lazy { arguments?.getInt("position", 0)!! }
    private val homeViewModel by activityViewModels<HomeViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeFavoriteItemBinding.bind(view)
        initView()
        initGoogleMap()
    }

    private fun initView() {

        binding.rvPet.layoutManager = LinearLayoutManager(context,
            LinearLayoutManager.HORIZONTAL,false)
        binding.rvPet.adapter =
            PetAdapter(requireContext(), homeViewModel.walkList.value!![index].petList)

        binding.tvWalkdistancevalue.text =
            formatDistance(homeViewModel.walkList.value!![index].distance)
        binding.tvWalktimevalue.text =
            formatTimeMinSec(homeViewModel.walkList.value!![index].walktime)
        binding.tvWalkstarttimevalue.text =
            formatTimeHourMin(homeViewModel.walkList.value!![index].starttime)
        binding.tvWalkendtimevalue.text =
            formatTimeHourMin(homeViewModel.walkList.value!![index].endtime)
        binding.tvWalkdatevalue.text =
            formatDateYmd(homeViewModel.walkList.value!![index].starttime)
    }

    private fun initGoogleMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragmentstop) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            val lastWalk = homeViewModel.walkList.value!![index].currentLocation
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    homeViewModel.walkList.value!![index].pathpoint[0][0],
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
            addMarker(
                googleMap,
                R.drawable.ic_placeholder_start,
                homeViewModel.walkList.value!![homeViewModel.walkList.value!!.size - 1].pathpoint.first().first()
            )
            addMarker(
                googleMap,
                R.drawable.ic_placeholder_end,
                homeViewModel.walkList.value!![homeViewModel.walkList.value!!.size - 1].pathpoint.last().last()
            )

            for (path in homeViewModel.walkList.value!![index].pathpoint) {
                googleMap.addPolyline(
                    PolylineOptions().addAll(path)
                        .color(Color.parseColor("#${homeViewModel.colorCode}"))
                        .width(homeViewModel.lineWidthText.toFloat())
                )
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
