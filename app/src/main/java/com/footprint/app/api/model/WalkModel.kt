package com.footprint.app.api.model

import com.google.android.gms.maps.model.LatLng

data class WalkModel(
    val distance:Int,
    val walktime:Int,
    val address:String,
    val date:String,
    val pathpoint:MutableList<MutableList<LatLng>>
)
