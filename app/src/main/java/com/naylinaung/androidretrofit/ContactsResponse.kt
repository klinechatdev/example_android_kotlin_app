package com.naylinaung.androidretrofit

data class ContactsResponse (
    val success: Boolean,
    val message: String,
    val data: List<Contact>
)