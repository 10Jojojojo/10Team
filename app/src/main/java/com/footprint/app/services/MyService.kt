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
import com.footprint.app.ui.home.HomeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

    class MyService : Service() {

        private lateinit var locationCallback: LocationCallback
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private val CHANNEL_ID = "com.example.myapp.location_service_channel"
        override fun onCreate() {
            super.onCreate()
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            createNotificationChannel()
            val notification = getNotification()
            startForeground(1, notification)

            startLocationUpdates()
            return START_STICKY
        }

        private fun startLocationUpdates() {
            val locationRequest = LocationRequest.create().apply {
                interval = 100
                fastestInterval = 100
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult?.let {
                        for (location in it.locations) {
                            // 이 부분에서 위치 데이터를 처리합니다. 예를 들어, broadcast를 보낼 수 있습니다.
                            sendBroadcastWithLocation(location)
                        }
                    }
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }

        private fun sendBroadcastWithLocation(location: Location) {
            val intent = Intent("LOCATION_UPDATE")
            intent.putExtra("latitude", location.latitude)
            intent.putExtra("longitude", location.longitude)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
        private fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val manager = getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(serviceChannel)
            }
        }
        private fun getNotification(): Notification {
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Running...")
                .setSmallIcon(R.mipmap.ic_launcher) // 알림 아이콘 설정
                .setContentIntent(pendingIntent)
                .build()
        }
        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        override fun onDestroy() {
            super.onDestroy()
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }