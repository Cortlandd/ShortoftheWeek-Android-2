package com.cortlandwalker.shortoftheweek.networking

import retrofit2.http.GET
import retrofit2.http.Query

interface SotwApi {
    @GET("/api/v1/films")
    suspend fun films(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): FilmResponse

    @GET("/api/v1/news")
    suspend fun news(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): FilmResponse

    @GET("/api/v1/search")
    suspend fun search(
        @Query("q", encoded = true) query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): FilmResponse
}
