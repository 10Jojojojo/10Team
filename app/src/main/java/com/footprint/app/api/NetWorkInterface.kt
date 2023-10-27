package com.footprint.app.api

import com.footprint.app.api.serverdata.Location
import com.footprint.app.api.serverdata.PlaceData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NetWorkInterface {
    @GET("place/nearbysearch/json")
   fun getPlace(
        @Query("keyword") keyword:String, // 키워드는 검색어 내가 입력하는값
//        @Query("location") location: Location, // 로케이션은 현재 나의 위치
        @Query("location") location: String, // 로케이션은 현재 나의 위치
        @Query("radius") radius:Int, // 검색할 반경 정의
        @Query("type") type:String, // 키워드를 줄여주는 타입. 타입은 정해져있음
        @Query("key") key:String, // 구글 API 키
//        @Query("PageToken") PageToken:String? // 다음 페이지를 검색하기 위한 토큰
        @Query("pagetoken") nextPageToken: String? = null
    ): Call<PlaceData?>?
}

/*
1. 기본 주소 : https://maps.googleapis.com/maps/api/place/nearbysearch/json

2. 요청 파라미터 :
필수 파라미터
location : 장소 정보를 검색할 중심 위치. 위도와 경도, 즉 latitude,longitude로 표시해야 한다.
radius : 중심 위치로부터 장소 결과를 반환할 반경(미터 단위)을 정의
// 우리 지도의 배율에 맞춰 계속 받아와야할지, 혹은 최대로 한번만 받아오면 되는건지
// 주변 검색으로 하면 됨.

선택적 파라미터
keyword : 검색할 텍스트 문자열, 예: "레스토랑" 또는 "123 메인 스트리트". 이것은 반드시 장소 이름, 주소 또는 업소 카테고리여야 합니다.
language : 결과를 반환할 언어입니다. 지원되는 언어 목록을 참조하십시오.

3. 검색 예시 :
https://maps.googleapis.com/maps/api/place/nearbysearch/json
  ?keyword=cruise
  &location=-33.8670522%2C151.1957362
  &radius=1500
  &type=restaurant
  &key=YOUR_API_KEY

4. Place types 확인하기
https://developers.google.com/maps/documentation/places/web-service/supported_types?hl=ko

type
: 결과를 지정된 유형과 일치하는 장소로 제한합니다. 지원되는 유형의 목록을 참조하십시오.

4-1. Place types에 병원은 있는데, 동물병원은 없음. 이럴때 데이터 찾는 방법
구글 Places API에서 특정 장소 유형(예: 동물병원, 애견 상점)이 명시적으로 목록에 없을 경우, 검색어(query)를 활용하여 보다 구체적인 검색을 수행할 수 있습니다.

{
  "location": "당신의 위도,경도",
  "radius": "검색 반경(m)",
  "type": "store",
  "keyword": "애견 상점",
  "key": "당신의 API 키"
}
type은 상점으로
keyword는 애견상점으로 하면 된다. keyword를 통해 세부적인 검색이 가능하다.
예시
beauty_salon - 미용실 을 통해 keyword로 애견미용실,
hospital - 병원 을 통해 keyword로 애견병원,
cafe - 카페 을 통해 keyword로 애견카페,
pet_store - 애완 동물점 을 통해 keyword로 애견상점

5. 추가 결과 액세스(Next Token 이용)
https://developers.google.com/maps/documentation/places/web-service/search-nearby?hl=ko#PlaceSearchPaging


API 받아오기 - location 지정(X) 주변위치 탐색(O)
적절한 키워드 설정(type과 키워드 겹치지 않게, 사실 keyword가 중요함. type은 keyword 이후 한번더 분류해주는 느낌

1. 키워드,로케이션 잘 받아와지는지 확인하기

2. 넥스트페이지토큰 동작하게 하기

3. 받아와지면, 모든 받아온 로케이션에 마커 찍기

4. 마커 찍히면 키워드별로 다르게 마커 찍기

5. 키워드별로 다르게 마커 찍히면 마커 다양하게 바꿔보기

6. 마커 다양하게 바뀌면 추가적인 자료를 받아와서 마커 누르면 해당 지점에 대한 자료 볼 수 있도록 하기


데이터 옮기기

1. 지도에서 버튼 누르면 경로와 지도크기, 카메라 위치 등등 필요한 데이터 저장하기

2. 데이터 저장이 되면, 게시글 커뮤니티에서도 지도를 띄우고, 띄울때 빌더를 통해 필요한 데이터 주입하기


*
2번째 지도도 권한이 필요한지 확인해보기
 */