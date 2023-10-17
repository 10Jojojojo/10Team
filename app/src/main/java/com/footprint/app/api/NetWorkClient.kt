package com.footprint.app.api

import com.footprint.app.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetWorkClient {
    val apiService :NetWorkInterface = instance.create(NetWorkInterface::class.java)

    private fun createOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        // 디버그 모드에서는 HTTP 요청 및 응답 내용을 로그에 출력
        if (BuildConfig.DEBUG)
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        else
            interceptor.level = HttpLoggingInterceptor.Level.NONE

        // OkHttpClient를 생성하고 설정을 추가
        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addNetworkInterceptor(interceptor)
            .build()
    }

    private val instance: Retrofit
        private get() {
            val gson = GsonBuilder().setLenient().create()

            return Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(createOkHttpClient())// OkHttpClient를 설정
                .build()
        }
}