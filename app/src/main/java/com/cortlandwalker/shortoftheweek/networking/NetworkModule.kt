package com.cortlandwalker.shortoftheweek.networking

import android.content.Context
import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttp(@ApplicationContext context: Context): OkHttpClient {
        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, 50L * 1024L * 1024L) // 50MB

        return OkHttpClient.Builder()
            .cache(cache)
            .callTimeout(25, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request()
                val urlStr = req.url().toString()
                val isSearch = req.url().encodedPath().contains("/api/v1/search")
                if (isSearch) {
                    Log.d("Net", "SEARCH_URL len=${urlStr.length} url=$urlStr")
                } else {
                    Log.d("Net", "${req.method()} ${req.url().encodedPath()} (urlLen=${urlStr.length})")
                }
                chain.proceed(req)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttp: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.shortoftheweek.com/")
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory(okhttp3.MediaType.get("application/json")))
            .build()

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): SotwApi = retrofit.create(SotwApi::class.java)
}
