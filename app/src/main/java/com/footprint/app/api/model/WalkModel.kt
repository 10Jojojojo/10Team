package com.footprint.app.api.model

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth

data class WalkModel(
    var petList: MutableList<PetInfoWalkModel> = mutableListOf(),
    var distance: Int = 0,
    var walktime: Long = 0L,
    var pathpoint: MutableList<MutableList<LatLng>> = mutableListOf(),
    var currentLocation: CameraPosition = CameraPosition(LatLng(0.0,0.0),0f,0f,0f),
    var snapshotPath: String = "",  // 스냅샷 파일의 경로 저장
    var starttime: Long = 0L,
    var endtime: Long = 0L,
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
    var uid:String? = null,
    var petList: MutableList<PetInfoWalkModel>? = null,
    var petKey: String? = null,
    var distance: Int? = null,
    var walktime: Long? = null,
    var pathpoint: MutableList<MutableList<LatLngDTO>>? = null,
    var currentLocation: CameraPositionDTO? = null,
    var snapshotPath: String? = null,  // 스냅샷 파일의 경로 저장
    var starttime: Long? = null,
    var endtime: Long? = null,
)

fun WalkModel.toDTO(): WalkModelDTO {
    // 'pathpoint'의 타입을 List<List<LatLngDTO>>로 변환
    val pathpointDTO = pathpoint.map { list ->
        list.map { latLng ->
            LatLngDTO(latLng.latitude, latLng.longitude)
        }.toMutableList()
    }.toMutableList()

    // 'currentLocation'을 CameraPositionDTO로 변환
    val currentLocationDTO = currentLocation.let {
        CameraPositionDTO(
            target = LatLngDTO(
                latitude = it.target.latitude,
                longitude = it.target.longitude
            ),
            zoom = it.zoom,
            tilt = it.tilt,
            bearing = it.bearing
        )
    }

    // 'WalkModelDTO' 인스턴스 생성
    return WalkModelDTO(
        uid = FirebaseAuth.getInstance().currentUser?.uid,
        petList = this.petList,
        distance = this.distance,
        walktime = this.walktime,
        pathpoint = pathpointDTO,
        currentLocation = currentLocationDTO,
        snapshotPath = this.snapshotPath,
        starttime = this.starttime,
        endtime = this.endtime
    )
}

fun WalkModelDTO.toModel(): WalkModel {
    val pathpointModel = pathpoint?.map { list ->
        list.map { dto ->
            LatLng(dto.latitude, dto.longitude)
        }.toMutableList()
    }?.toMutableList() ?: mutableListOf()

    val currentLocationModel = currentLocation?.let {
        CameraPosition(
            LatLng(it.target.latitude, it.target.longitude),
            it.zoom,
            it.tilt,
            it.bearing
        )
    } ?: CameraPosition(LatLng(0.0,0.0), 0f, 0f, 0f)

    // 'WalkModel' 인스턴스 생성
    return WalkModel(
        petList = this.petList ?: mutableListOf(),
        distance = this.distance ?: 0,
        walktime = this.walktime ?: 0L,
        pathpoint = pathpointModel,
        currentLocation = currentLocationModel,
        snapshotPath = this.snapshotPath ?: "",
        starttime = this.starttime ?: 0L,
        endtime = this.endtime ?: 0L
        // 존재하지 않는 필드는 제거합니다.
    )
}