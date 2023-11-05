package com.footprint.app.api.model

import android.net.Uri
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WalkModel(
    var key:String = "",
    val distance: String,
    val walktime: String,
    val pathpoint: MutableList<MutableList<LatLng>>,
    val currentLocation: CameraPosition,
    var snapshotPath: String? = null,  // 스냅샷 파일의 경로 저장
    var petimage: Uri? = null, // 산책한 반려동물 사진
    val name: String = "내새끼",
    var starttime: String = "",
    var endtime: String = "",
    val date: String = SimpleDateFormat("yy년 MM월 dd일", Locale.KOREA).format(Date()),
    val dateid: String = SimpleDateFormat("yyMMddHHmmss", Locale.KOREA).format(Date())
)

data class CameraPositionDTO(
    var target: LatLngDTO = LatLngDTO(),
    var zoom: Float = 0f,
    var tilt: Float = 0f,
    var bearing: Float = 0f
)

data class LatLngDTO(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)

data class WalkModelDTO(
    var key:String? = null,
    var distance: String = "",
    var walktime: String = "",
    var pathpoint: List<List<LatLngDTO>> = listOf(),
    var currentLocation: CameraPositionDTO = CameraPositionDTO(),
    var snapshotPath: String? = null,
    var name: String = "",
    var starttime: String = "",
    var endtime: String = "",
    var date: String = "",
    var dateid: String = "",
)

fun WalkModel.toDTO(): WalkModelDTO {
    val pathpointDTO = pathpoint.map { list ->
        list.map { latLng ->
            LatLngDTO(latLng.latitude, latLng.longitude)
        }
    }
    val currentLocationDTO = CameraPositionDTO(
        target = LatLngDTO(
            latitude = currentLocation.target.latitude,
            longitude = currentLocation.target.longitude
        ),
        zoom = currentLocation.zoom,
        tilt = currentLocation.tilt,
        bearing = currentLocation.bearing
    )
    return WalkModelDTO(
        key = this.key,
        distance = this.distance,
        walktime = this.walktime,
        pathpoint = pathpointDTO,
        currentLocation = currentLocationDTO,
        snapshotPath = this.snapshotPath,
        name = this.name,
        starttime = this.starttime,
        endtime = this.endtime,
        date = this.date,
        dateid = this.dateid
    )
}

fun WalkModelDTO.toModel(): WalkModel {
    val pathpointModel = pathpoint.map { list ->
        list.map { dto ->
            LatLng(dto.latitude, dto.longitude)
        }.toMutableList()
    }.toMutableList()
    val currentLocationModel = CameraPosition.Builder()
        .target(LatLng(currentLocation.target.latitude, currentLocation.target.longitude))
        .zoom(currentLocation.zoom)
        .tilt(currentLocation.tilt)
        .bearing(currentLocation.bearing)
        .build()
    return WalkModel(
        key = this.key ?: "",
        distance = this.distance,
        walktime = this.walktime,
        pathpoint = pathpointModel,
        currentLocation = currentLocationModel,
        snapshotPath = this.snapshotPath,
        name = this.name,
        starttime = this.starttime,
        endtime = this.endtime,
        date = this.date,
        dateid = this.dateid
    )
}