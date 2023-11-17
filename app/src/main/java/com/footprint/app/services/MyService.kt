package com.footprint.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.footprint.app.MainActivity
import com.footprint.app.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY

class MyService : Service() {

    private lateinit var locationCallback: LocationCallback

    // 기기 위치 정보를 가져 오는 여러 메서드 를 제공 하는 FusedLocationProviderClient instance 를 참조 하기 위한 변수
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // 알림 채널 ID 설정
    private val channelId = "com.example.myapp.location_service_channel"
    override fun onCreate() {
        super.onCreate()
        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 위치 update 시작
        startLocationUpdates()
        // 알림 채널 생성 및 서비스 를 foreground 상태로 만들기
        createNotificationChannel()
        val notification = getNotification()
        startForeground(1, notification)
        return START_STICKY
    }

    // 주기 적인 위치 update 를 시작 하는 함수
    private fun startLocationUpdates() {
//            val locationRequest = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 500L)
//                .setMinUpdateIntervalMillis(250L)
//                .build()
        val locationRequest = LocationRequest.Builder(500L).apply {
            setIntervalMillis(500L)
            setMinUpdateIntervalMillis(250L)
            setPriority(PRIORITY_HIGH_ACCURACY)
        }.build() // 나중에는 0.5, 0.25초말고 2~3초로 늘리기 (배터리광탈한다)
        // 위치가 update 될 때마다 호출될 콜백 설정
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.let {
                    for (location in it.locations) {
                        // 이 부분 에서 위치 데이터 를 처리 한다. 예를 들어, broadcast 를 보낼 수 있다.
                        sendBroadcastWithLocation(location)
                    }
                }
            }
        }
        // 위치 update 시작
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // 받은 위치 데이터 를 기반 으로 Broadcast 를 전송 하는 함수
    private fun sendBroadcastWithLocation(location: Location) {
        val intent = Intent("LOCATION_UPDATE")
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    // 알림 채널을 생성 하는 함수
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    // 서비스 의 foreground 알림을 반환 하는 함수
    private fun getNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("산책 중 입니다.")
            .setContentText("산책 중")
            .setSmallIcon(R.drawable.ic_pawprint_on) // 알림 아이콘 설정
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // 서비스 가 종료될 때 위치 update 를 중단
    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}