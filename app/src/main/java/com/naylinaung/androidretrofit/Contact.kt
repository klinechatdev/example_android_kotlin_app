package com.naylinaung.androidretrofit

data class Contact(
    val completed: Boolean,
    val id: String,
    val name: String,
    val token: String,
    val created_at: Long
)