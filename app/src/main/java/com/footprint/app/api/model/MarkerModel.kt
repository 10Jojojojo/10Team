package com.footprint.app.api.model

import com.footprint.app.Constants.BIND_OFF
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth

data class MarkerModel(
    var markerKey:String? = "",
    var markerdrawable:Int = 0 ,
    var text:String = "",
    var latlng: LatLng = LatLng(0.0,0.0),
    var bindState:Int = BIND_OFF,
    var marker: Marker? = null  // Marker 객체에 대한 참조
)
data class MarkerModelDTO(
    var markerKey:String? = "",
    var uid:String? = "",
    var markerdrawable:Int = 0,
    var text:String = "",
    var latlng: LatLngDTO = LatLngDTO(), // LatLng 객체의 DTO 버전
    var marker: Marker? = null  // Marker 객체에 대한 참조
)

fun MarkerModel.toDTO(): MarkerModelDTO {
    return MarkerModelDTO(
        uid = FirebaseAuth.getInstance().currentUser?.uid,
        markerdrawable = this.markerdrawable,
        text = this.text,
        latlng = LatLngDTO(this.latlng.latitude, this.latlng.longitude)
    )
}
fun MarkerModelDTO.toModel(): MarkerModel {
    return MarkerModel(
        markerKey = this.markerKey,
        markerdrawable = this.markerdrawable,
        text = this.text,
        bindState = BIND_OFF,
        latlng = LatLng(this.latlng.latitude, this.latlng.longitude)
    )
}