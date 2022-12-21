package com.klinechat.klinechatwebrtcexample.api

import com.klinechat.klinechatwebrtcexample.ContactApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: ContactApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://webrtcapi.naylinaung.asia")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ContactApi::class.java)
    }
}