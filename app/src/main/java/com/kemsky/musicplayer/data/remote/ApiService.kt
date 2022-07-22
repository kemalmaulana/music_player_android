package com.kemsky.musicplayer.data.remote

import com.google.gson.GsonBuilder
import com.kemsky.musicplayer.constant.AppConstant.BASE_URL
import com.kemsky.musicplayer.data.model.SearchMusicModel
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService {

    @GET("/search")
    suspend fun searchArtist(
        @Query("term") term: String,
        @Query("entity") entity: String
    ): Response<SearchMusicModel>

    companion object {
        private const val TIME_OUT = 60_000
        operator fun invoke(): ApiService {
            val gson = GsonBuilder().create()
            val client = OkHttpClient.Builder()
                .connectTimeout(TIME_OUT.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(TIME_OUT.toLong(), TimeUnit.MILLISECONDS)
                .writeTimeout(TIME_OUT.toLong(), TimeUnit.MILLISECONDS)
                .build()

            return Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(ApiService::class.java)
        }
    }

}