package com.footprint.app.api.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

data class FlagModel(
    var key:String = "",
    val flag:Int,
    val text:String,
    val latlng: LatLng,
    var marker: Marker? = null  // Marker 객체에 대한 참조
)
data class FlagModelDTO(
    var key: String? = null,
    var flag: Int = 0 ,
    var text: String = "",
    var latlng: LatLngDTO = LatLngDTO() // LatLng 객체의 DTO 버전
)

fun FlagModel.toDTO(): FlagModelDTO {
    return FlagModelDTO(
        key = this.key,
        flag = this.flag,
        text = this.text,
        latlng = LatLngDTO(this.latlng.latitude, this.latlng.longitude)
    )
}
fun FlagModelDTO.toModel(): FlagModel {
    return FlagModel(
        key = this.key ?: "",
        flag = this.flag,
        text = this.text,
        latlng = LatLng(this.latlng.latitude, this.latlng.longitude)
    )
}