package kc.ac.uc.clubplatform.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kc.ac.uc.clubplatform.BuildConfig

object ApiClient {
    private val SERVER_BASE_URL = BuildConfig.SERVER_BASE_URL

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val serverRetrofit = Retrofit.Builder()
        .baseUrl(SERVER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val userService: UserService = serverRetrofit.create(UserService::class.java)
    val apiService: ApiService = serverRetrofit.create(ApiService::class.java)
}
