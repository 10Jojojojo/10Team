package com.footprint.app.api.model


data class ProfileModel(
    var key:String = "",
    val nickName:String,
    val dogName:String,
    val dogAgeText:String,
    val dogAge:Int,
    val dogSex:String,
    val markerList:List<FlagModel>,
    val selectedImageUri: String
)

data class ProfileModelDTO(
    var key:String? = null,
    var nickName: String = "",
    var dogName: String = "",
    var dogAgeText: String = "",
    var dogAge: Int = 0,
    var dogSex: String = "",
    var markerList: List<FlagModelDTO> = listOf(),
    var selectedImagePath: String = "" // Uri 대신 이미지 경로를 String으로 저장
)

fun ProfileModel.toDTO(): ProfileModelDTO {
    return ProfileModelDTO(
        key = this.key,
        nickName = this.nickName,
        dogName = this.dogName,
        dogAgeText = this.dogAgeText,
        dogAge = this.dogAge,
        dogSex = this.dogSex,
        markerList = this.markerList.map { it.toDTO() },
        selectedImagePath = this.selectedImageUri
    )
}
fun ProfileModelDTO.toModel(): ProfileModel {
    return ProfileModel(
        key = this.key ?: "",
        nickName = this.nickName,
        dogName = this.dogName,
        dogAgeText = this.dogAgeText,
        dogAge = this.dogAge,
        dogSex = this.dogSex,
        markerList = this.markerList.map { it.toModel() },
        selectedImageUri = this.selectedImagePath
    )
}
