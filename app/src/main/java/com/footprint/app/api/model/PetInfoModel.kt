package com.footprint.app.api.model

data class PetInfoModel(
    var key:String? = null,
    val timestamp: Long = 0L,
    var petImageUrl:String? = null,
    var petName:String = "",
    var petAge:String = "",
    var petSex:String = "",
    var activePet: Boolean = false
)
data class PetInfoModelDTO(
    var key:String? = null,
    val timestamp: Long = 0L,
    val petImageUrl: String? = null,
    val petName: String = "",
    val petAge: String = "",
    val petSex: String = "",
    var activePet: Boolean = false
)

fun PetInfoModel.toDTO(): PetInfoModelDTO {
    return PetInfoModelDTO(
        timestamp = this.timestamp,
        petImageUrl = this.petImageUrl,
        petName = this.petName,
        petAge = this.petAge,
        petSex = this.petSex,
        activePet = this.activePet
    )
}
fun PetInfoModelDTO.toModel(): PetInfoModel {
    return PetInfoModel(
        key = this.key,
        timestamp = this.timestamp,
        petImageUrl = this.petImageUrl,
        petName = this.petName,
        petAge = this.petAge,
        petSex = this.petSex,
        activePet = this.activePet
    )
}
data class PetInfoWalkModel(
    var petName:String = "",
    var petImageUrl:String? = null,
)