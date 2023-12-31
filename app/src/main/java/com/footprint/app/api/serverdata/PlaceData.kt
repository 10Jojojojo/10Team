package com.footprint.app.api.serverdata

data class PlaceData(
    val html_attributions: List<String>,
    var next_page_token: String,
    val results: List<PlaceResult>
)

data class PlaceResult(
    val business_status: String,
    val geometry: Geometry,
    val icon: String,
    val icon_background_color: String,
    val icon_mask_base_uri: String,
    val name: String,
    val opening_hours: OpeningHours,
    val photos: List<Photo>,
    val place_id: String,
    val plus_code: PlusCode,
    val price_level: Int,
    val rating: Float,
    val reference: String,
    val scope: String,
    val types: List<String>,
    val user_ratings_total: Int,
    val vicinity: String
)

data class Geometry(
    val location: Location,
    val viewport: Viewport
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Viewport(
    val northeast: Coordinate,
    val southwest: Coordinate
)

data class Coordinate(
    val lat: Double,
    val lng: Double
)

data class OpeningHours(
    val open_now: Boolean
)

data class Photo(
    val height: Int,
    val html_attributions: List<String>,
    val photo_reference: String,
    val width: Int
)

data class PlusCode(
    val compound_code: String,
    val global_code: String
)
