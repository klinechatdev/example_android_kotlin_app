package com.klinechat.webrtcsdk.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.hide(): LiveData<T> = this