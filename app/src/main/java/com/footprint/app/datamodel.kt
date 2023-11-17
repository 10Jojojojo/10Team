package com.footprint.app

import com.google.firebase.database.IgnoreExtraProperties
@IgnoreExtraProperties
class DogProfile {
    var name: String? = null
    var breed: String? = null
    var age = 0
    var gender: String? = null

    // 기본 생성자
    constructor()

    // 추가적인 생성자
    constructor(name: String?, breed: String?, age: Int, gender: String?) {
        this.name = name
        this.breed = breed
        this.age = age
        this.gender = gender
    }
}