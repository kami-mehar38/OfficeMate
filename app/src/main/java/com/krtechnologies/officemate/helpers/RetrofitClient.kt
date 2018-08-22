package com.krtechnologies.officemate.helpers

import android.annotation.SuppressLint
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit


/**
 * This project is created by Kamran Ramzan on 22-Aug-18.
 */
class RetrofitClient {

    private var retrofit: Retrofit? = null


    companion object {
        @SuppressLint("StaticFieldLeak")
        private var retrofitClient: RetrofitClient? = null

        @Synchronized
        @JvmStatic
        fun getInstance(): RetrofitClient {
            synchronized(Helper::class) {
                if (retrofitClient == null)
                    retrofitClient = RetrofitClient()
                return retrofitClient!!
            }
        }
    }

    fun getClient(baseUrl: String): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
        return retrofit!!
    }
}