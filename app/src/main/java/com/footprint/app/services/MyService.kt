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

    class MyService : Service() {

        private lateinit var locationCallback: LocationCallback

        // 기기 위치 정보를 가져오는 여러 메서드를 제공하는 FusedLocationProviderClient 인스턴스를 참조하기 위한 변수
        private lateinit var fusedLocationClient: FusedLocationProviderClient

        // 알림 채널 ID 설정
        private val channelId = "com.example.myapp.location_service_channel"
        override fun onCreate() {
            super.onCreate()
            // FusedLocationProviderClient 초기화
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            // 위치 업데이트 시작
            startLocationUpdates()
            // 알림 채널 생성 및 서비스를 foreground 상태로 만들기
            createNotificationChannel()
            val notification = getNotification()
            startForeground(1, notification)
            return START_STICKY
        }
        // 주기적인 위치 업데이트를 시작하는 함수
        private fun startLocationUpdates() {
            val locationRequest = LocationRequest().apply {
                interval = 500
                fastestInterval = 500
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            // 위치가 업데이트될 때마다 호출될 콜백 설정
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.let {
                        for (location in it.locations) {
                            // 이 부분에서 위치 데이터를 처리한다. 예를 들어, broadcast를 보낼 수 있다.
                            sendBroadcastWithLocation(location)
                        }
                    }
                }
            }
            // 위치 업데이트 시작
            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }

        // 받은 위치 데이터를 기반으로 Broadcast를 전송하는 함수
        private fun sendBroadcastWithLocation(location: Location) {
            val intent = Intent("LOCATION_UPDATE")
            intent.putExtra("latitude", location.latitude)
            intent.putExtra("longitude", location.longitude)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }

        // 알림 채널을 생성하는 함수
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

        // 서비스의 foreground 알림을 반환하는 함수
        private fun getNotification(): Notification {
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Builder(this, channelId)
                .setContentTitle("산책 중입니다.")
                .setContentText("산책 중")
                .setSmallIcon(R.drawable.ic_pawprint_on) // 알림 아이콘 설정
                .setContentIntent(pendingIntent)
                .build()
        }
        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        // 서비스가 종료될 때 위치 업데이트를 중단
        override fun onDestroy() {
            super.onDestroy()
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }