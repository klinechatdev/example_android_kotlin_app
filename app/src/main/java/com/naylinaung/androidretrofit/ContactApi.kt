package com.naylinaung.androidretrofit

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ContactApi {

    @GET("/contacts")
    suspend fun getContacts(@Query("device_id") device_id: String): Response<ContactsResponse>

    @POST("/contacts")
    suspend fun saveToken(@Body params: SaveTokenRequest): Response<SaveTokenResponse>

    @POST("/call_contact")
    suspend fun callContact(@Body params: CallContactRequest): Response<CallContactResponse>


}