    package com.footprint.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MyService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Started Service에서 서비스 시작시 호출
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onBind(intent: Intent): IBinder {
        // Bound Service에서 서비스 연결시 호출
        TODO("Return the communication channel to the service.")
    }
}