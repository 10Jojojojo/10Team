package com.footprint.app.api.model

import android.net.Uri

data class ProfileModel(
    val nickName:String,
    val dogName:String,
    val dogAgeText:String,
    val dogAge:Int,
    val dogSex:String,
    val selectedImageUri: Uri?
)

