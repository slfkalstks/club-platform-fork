package kc.ac.uc.clubplatform.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://www.career.go.kr/cnet/openapi/"
    private const val SERVER_BASE_URL = "http://hide-ipv4.xyz/api/"
    const val API_KEY = "f2f6128e7cab84a695ad3ff381ff6b04"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        
    private val serverRetrofit = Retrofit.Builder()
        .baseUrl(SERVER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val schoolApiService: SchoolApiService = retrofit.create(SchoolApiService::class.java)
    val userService: UserService = serverRetrofit.create(UserService::class.java)
}
