package com.footprint.app.api.model

import com.google.android.gms.maps.model.LatLng

data class FlagModel(
    val flag:Int,
    val text:String,
    val latlng: LatLng
)
