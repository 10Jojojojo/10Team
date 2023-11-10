package com.footprint.app.api.model


data class ProfileModel(
    val nickName:String? = null,
    val profileImageUri: String? = null,
    val address: String? = null,
    val introduction:String? = null,
    val petInfoModelList: MutableList<PetInfoModel>? = null
)

data class ProfileModelDTO(
    val nickName: String? = null,
    val profileImageUri: String? = null,
    val address: String? = null,
    val introduction: String? = null,
    val petInfo: Map<String, PetInfoModelDTO>? = null
)

fun ProfileModel.toDTO(): ProfileModelDTO {
    // List의 각 요소를 Map의 Entry로 변환
    val petInfoMap = this.petInfoModelList?.associateBy { it.key ?: "" }
        ?.mapValues { it.value.toDTO() }

    return ProfileModelDTO(
        nickName = this.nickName,
        profileImageUri = this.profileImageUri,
        address = this.address,
        introduction = this.introduction,
        petInfo = petInfoMap ?: emptyMap()
    )
}
fun ProfileModelDTO.toModel(): ProfileModel {
    // petInfo Map을 List로 변환
    val petInfoList = this.petInfo?.values?.map { it.toModel() }?.toMutableList()

    return ProfileModel(
        nickName = this.nickName,
        profileImageUri = this.profileImageUri,
        address = this.address,
        introduction = this.introduction,
        petInfoModelList = petInfoList ?: mutableListOf()
    )
}