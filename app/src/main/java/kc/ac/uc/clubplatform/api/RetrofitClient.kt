package kc.ac.uc.clubplatform.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 명시적으로 프로토콜을 http로 지정
    private const val BASE_URL = "http://hide-ipv4.xyz/api/"
    // 추후 IP 주소 업데이트

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)  // 연결 제한시간 늘림
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        // DNS 문제 해결을 위한 설정 추가
        .dns(object : okhttp3.Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                return try {
                    // 표준 DNS 조회 시도
                    InetAddress.getAllByName(hostname).toList()
                } catch (e: Exception) {
                    // 도메인 해석 실패 시 IP 주소 직접 사용
                    if (hostname == "hide-ipv4.xyz") {
                        try {
                            // 서버 IP 주소를 직접 지정
                            listOf(InetAddress.getByName("35.193.194.46:8080"))
                        } catch (e: Exception) {
                            throw UnknownHostException("Unable to resolve host $hostname")
                        }
                    } else {
                        throw UnknownHostException("Unable to resolve host $hostname")
                    }
                }
            }
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}