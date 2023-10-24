package com.footprint.app.api.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

data class FlagModel(
    val flag:Int,
    val text:String,
    val latlng: LatLng,
    var marker: Marker? = null  // Marker 객체에 대한 참조
)
