package com.naylinaung.androidretrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: ContactApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://webrtcproxy.codingelephant.tech")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ContactApi::class.java)
    }
}