package com.krtechnologies.officemate.interfaces

import com.krtechnologies.officemate.models.Admin
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


/**
 * This project is created by Kamran Ramzan on 22-Aug-18.
 */
interface APIService {

    /*@POST("/api/admin")
    @FormUrlEncoded
    fun signUpAdmin(@Field("name") name: String,
                 @Field("profile_picture") profilePicture: String,
                 @Field("email") email: String,
                 @Field("password") password: String,
                 @Field("organization") organization: String,
                 @Field("designation") designation: String,
                 @Field("joining_date") joining_date: String,
                 @Field("subscription") subscription: String,
                 @Field("token") token: String): Call<Admin>*/

    @POST("/api/admin")
    fun signUpAdmin(@Body admin: Admin): Call<String>
}