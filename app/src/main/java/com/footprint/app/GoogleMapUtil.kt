package com.footprint.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.footprint.app.api.model.FlagModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

object GoogleMapUtil {
    fun addMarkerAndText(
        context: Context,
        mGoogleMap: GoogleMap,
        latlng: LatLng,
        resourceId: Int,
        textValue:String,
        textColor:Int = Color.BLUE,
        textBold:Typeface? = Typeface.DEFAULT_BOLD,
        imageWidth: Int = 100,
        imageHeight: Int = 100,
        textsize: Float = 30f,
    ) :Marker? {
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
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
    fun addMarker(
        context: Context,
        mGoogleMap: GoogleMap,
        latlng: LatLng,
        resourceId: Int,
        textValue:String,
        imageWidth: Int = 100,
        imageHeight: Int = 100
    ) :Marker? {
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, false)
        val customMarker = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        val markerOptions = MarkerOptions().position(latlng).title(textValue).icon(customMarker)
        return mGoogleMap.addMarker(markerOptions)
    }
    fun addFlagsToMap(
        context: Context,
        mGoogleMap: GoogleMap,
        flags: MutableList<FlagModel>,
        imageWidth: Int = 100,
        imageHeight: Int = 100
    ) {
        // 모든 플래그 모델을 순회하며 마커를 추가합니다.
        flags.forEach { flagModel ->
            // 이미지 리소스 ID를 사용하여 마커를 생성합니다.
            val marker = addMarker(
                context = context,
                mGoogleMap = mGoogleMap,
                latlng = flagModel.latlng,
                resourceId = flagModel.flag,
                textValue = flagModel.text,
                imageWidth = imageWidth,
                imageHeight = imageHeight
            )
            // 생성된 마커를 FlagModel에 저장합니다.
            flagModel.marker = marker
        }
    }

}