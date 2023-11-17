package com.footprint.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.footprint.app.Constants.BIND_ON
import com.footprint.app.api.model.MarkerModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//object GoogleMapUtil {
// GoogleMapUitl 이란 컨벤션은 안쓰임
// 애초에 오브젝트를 주석처리해도 전역함수로 잘만 쓰임
// Context같은경우 파라미터로 넣지 않는게 좋음. 확장함수로 쓰면됨 Context. 를 함수에 붙이고, 실행문에 context를 this로 바꿔주면됨
fun Context.addMarkerAndText(
//        context: ,
    mGoogleMap: GoogleMap,
    latlng: LatLng,
    resourceId: Int,
    textValue: String,
    textColor: Int = Color.BLUE,
    textBold: Typeface? = Typeface.DEFAULT_BOLD,
    imageWidth: Int = 100,
    imageHeight: Int = 100,
    textsize: Float = 30f,
): Marker? {
    val bitmap = BitmapFactory.decodeResource(this.resources, resourceId)
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, false)

    // 텍스트를 그리기 위한 Canvas를 생성합니다.
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor // 텍스트 색상 설정
        textSize = textsize // 텍스트 사이즈 설정
        typeface = textBold // 텍스트 스타일 설정
    }
    // 텍스트의 너비를 측정합니다.
    val text = textValue
    val textWidth = paint.measureText(text)

    // 텍스트를 비트맵의 중앙에 그리기 위해 x, y 좌표를 계산합니다.
//            val x = scaledBitmap.width / 2f

    val x = (scaledBitmap.width - textWidth) / 2f
    val y = (scaledBitmap.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)

    // 숫자를 마커 이미지 위에 그립니다.
    val canvas = Canvas(scaledBitmap)
    canvas.drawText(textValue, x, y, paint)
    val customMarker = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    val markerOptions = MarkerOptions().position(latlng).title(textValue).icon(customMarker)
    return mGoogleMap.addMarker(markerOptions)
}

fun Context.addMarker(
    mGoogleMap: GoogleMap,
    latlng: LatLng,
    resourceId: Int,
    textValue: String,
    imageWidth: Int = 100,
    imageHeight: Int = 100,
    onCompleted: (MarkerModel) -> Unit
) {
    val bitmap = BitmapFactory.decodeResource(this.resources, resourceId)
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, false)
    val customMarker = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    val markerOptions = MarkerOptions().position(latlng).title(textValue).icon(customMarker)
    onCompleted(
        MarkerModel(
            bindState = BIND_ON,
            markerdrawable = resourceId,
            text = textValue,
            latlng = latlng,
            marker = mGoogleMap.addMarker(markerOptions)
        )
    )
}
fun Context.addFlagsToMap(
    mGoogleMap: GoogleMap,
    markers: MutableList<MarkerModel>,
    imageWidth: Int = 100,
    imageHeight: Int = 100
) {
    // 모든 플래그 모델을 순회하며 마커를 추가합니다.
    markers.forEach {markermodel->
        // 이미지 리소스 ID를 사용하여 마커를 생성합니다.
        this.addMarker(
            mGoogleMap = mGoogleMap,
            latlng = markermodel.latlng,
            resourceId = R.drawable.ic_flag,
            textValue = markermodel.text,
            imageWidth = imageWidth,
            imageHeight = imageHeight
        ) {markermodel.marker = it.marker}
        // 생성된 마커를 FlagModel에 저장합니다.
    }
}

fun Context.addFlagsToMap(
    mGoogleMap: GoogleMap,
    imarker: MarkerModel,
    imageWidth: Int = 100,
    imageHeight: Int = 100
) {
    // 이미지 리소스 ID를 사용하여 마커를 생성합니다.
    this.addMarker(
        mGoogleMap = mGoogleMap,
        latlng = imarker.latlng,
        resourceId = imarker.markerdrawable,
        textValue = imarker.text,
        imageWidth = imageWidth,
        imageHeight = imageHeight
    ) {imarker.marker = it.marker}
    // 생성된 마커를 FlagModel에 저장합니다.
}

fun formatDistance(distanceInMeters: Int): String {
    return if (distanceInMeters < 100) {
        "$distanceInMeters m"
    } else {
        val distanceInKilometers = distanceInMeters / 1000.0
        String.format("%.2f km", distanceInKilometers)
    }
}

fun formatTimeHourMin(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("a hh:mm", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}

fun formatTimeMinSec(hourInMillis: Long): String {
    val totalSecs = hourInMillis / 1000
    val hours = totalSecs / 3600
    val minutes = (totalSecs % 3600) / 60
    val seconds = totalSecs % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun formatDateYmd(timeInMillis: Long): String {
    val dateFormat = SimpleDateFormat("yy년 MM월 dd일", Locale.KOREA)
    return dateFormat.format(Date(timeInMillis))
}
fun formatDateMd(timeInMillis: Long): String {
    val dateFormat = SimpleDateFormat("MM월 dd일", Locale.KOREA)
    return dateFormat.format(Date(timeInMillis))
}
fun formatDateMdhm(timeInMillis: Long): String {
    val dateFormat = SimpleDateFormat("MM월 dd일 hh : mm", Locale.KOREA)
    return dateFormat.format(Date(timeInMillis))
}

//    // 스냅샷을 찍고 PNG 파일을 반환하는 함수
//    fun captureMapSnapshot(googleMap: GoogleMap, context: Context, onSnapshotReady: (Uri?) -> Unit) {
//        googleMap.snapshot { bitmap ->
//            try {
//                // 외부 파일 저장 디렉토리에 "map_snapshot.png" 파일을 생성
//                val snapshotFile = File(context.getExternalFilesDir(null), "map_snapshot.png")
//                FileOutputStream(snapshotFile).use { out ->
//                    // 비트맵을 PNG 형식으로 파일에 저장
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
//                }
//                // 파일의 Uri를 콜백 함수에 전달
//                val snapshotUri = FileProvider.getUriForFile(
//                    context,
//                    context.packageName + ".provider", // 이 부분은 manifest에 정의된 file provider와 일치해야 함
//                    snapshotFile
//                )
//                onSnapshotReady(snapshotUri)
//            } catch (e: Exception) {
//                // 오류 처리
//                Log.e("Snapshot", "Error capturing map snapshot.", e)
//                onSnapshotReady(null)
//            }
//        }
//    }
// 스냅샷을 찍고 바이트 배열로 반환하는 함수
fun captureMapSnapshot(googleMap: GoogleMap, onCompleted: (ByteArray?) -> Unit) {
    googleMap.snapshot { bitmap ->
        // Bitmap을 ByteArrayOutputStream으로 변환
        ByteArrayOutputStream().use { baos ->
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val data = baos.toByteArray()
            onCompleted(data)
        }
    }
}
//}