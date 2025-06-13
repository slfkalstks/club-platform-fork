package kc.ac.uc.clubplatform.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // 에뮬레이터에서 로컬 서버 접근을 위한 IP 설정
//    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    // API 기본 URL - 여기서는 공개적으로 노출하여 다른 클래스에서 참조할 수 있도록 함
    const val BASE_URL = "https://hide-ipv4.xyz/api/"
    
    // 프로필 이미지 URL을 생성하는 헬퍼 메서드 (URL 구성 로직 중앙화)
    fun getProfileImageUrl(userId: String): String {
        return "${BASE_URL.trimEnd('/')}/auth/profile-image/$userId"
    }
    
    // OkHttpClient를 외부에서 접근할 수 있도록 함
    lateinit var okHttpClient: OkHttpClient
        private set

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Content-Type", "application/json")
            .method(original.method, original.body)
        
        chain.proceed(requestBuilder.build())
    }

    // 초기화 메서드 추가
    fun init(context: Context) {
        // 인증 토큰 인터셉터 생성
        val tokenInterceptor = Interceptor { chain ->
            val original = chain.request()
            
            // SharedPreferences에서 토큰 가져오기
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val accessToken = sharedPreferences.getString("access_token", null)
            
            // 토큰이 있으면 요청 헤더에 추가
            val requestBuilder = original.newBuilder()
            if (accessToken != null) {
                requestBuilder.header("Authorization", "Bearer $accessToken")
            }
            
            chain.proceed(requestBuilder.build())
        }
        
        // OkHttpClient 초기화
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(tokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
        
        // Retrofit 초기화
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(ApiService::class.java)
    }

    // Retrofit 객체를 lateinit으로 변경
    private lateinit var retrofit: Retrofit
    
    // ApiService도 lateinit으로 변경
    lateinit var apiService: ApiService
        private set
}
