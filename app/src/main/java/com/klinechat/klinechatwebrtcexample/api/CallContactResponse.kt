package com.klinechat.klinechatwebrtcexample

data class CallContactResponse(
    val success: Boolean,
    val message: String,
    val room_token: String?,
)
