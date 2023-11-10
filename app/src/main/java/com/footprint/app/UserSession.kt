//package com.footprint.app
//
//import com.footprint.app.FirebaseDatabaseManager.saveMarkerList
//import com.footprint.app.FirebaseDatabaseManager.saveProfile
//import com.footprint.app.FirebaseDatabaseManager.saveWalkList
//import com.footprint.app.api.model.MarkerModel
//import com.footprint.app.api.model.PostModel
//import com.footprint.app.api.model.ProfileModel
//import com.footprint.app.api.model.UserdataModel
//import com.footprint.app.api.model.WalkModel
//
//// 없어도 될 object임 로그인 한 이후에 FirebaseAuth.getInstance().currentUser.uid를 사용하면될듯.
////
//object UserSession {
//    // LiveData를 쓰는 이유가 값의 변화가 있는 시점에서
//    // userdata를 Livedata로 만들고 관찰하기.
//    private var userId: String = ""
//    private var userdata: UserdataModel = UserdataModel("")
//
//    fun updateUid(inUid:String){
//        userId = inUid
//    }
////    @Synchronized
//    // @Synchronized는 필요없음 언제 필요하냐면, 여러 쓰레드에서 동시접근 할 때 다중업데이트를 방지하려고 하는것.
//    // 레이스 컨디션 사용자 1,2 가 동시에 업데이트를 하면 동시접근을 하려하면 차단하는것
//    // 여기서는 Synchronized 가 아닌 코드의 순서를 잘 맞추는것으로 해결해야함.
//    fun updateUserdata(markers: MarkerModel) {
//        userdata.markers.add(markers)
//        saveMarkerList(userId,userdata.markers){}
//    }
//
//    fun updateUserdata(walks: WalkModel) {
//        userdata.walks.add(walks)
//        saveWalkList(userId,userdata.walks){}
//    }
//
//    fun updateUserdata(posts: PostModel) {
//        userdata.posts.add(posts)
//    }
//
//    fun updateUserdata(profile: ProfileModel) {
//        userdata.profile = profile
//        saveProfile(userId,profile){}
//    }
//    fun updateUserdata(inUserdata: UserdataModel) {
//        userdata = inUserdata
//    }
//    fun getUid() : String {
//        return userId
//    }
//    fun getUserdata() : UserdataModel {
//        return userdata
//    }
//
//    fun getWalks(): MutableList<WalkModel> {
//        return userdata.walks
//    }
//
//
//    fun getMarkers(): MutableList<MarkerModel> {
//        return userdata.markers
//    }
//
//    fun getPosts(): MutableList<PostModel> {
//        return userdata.posts
//    }
//
//    fun getProfile(): ProfileModel {
//        return userdata.profile
//    }
//
//}