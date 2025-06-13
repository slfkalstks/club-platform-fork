package kc.ac.uc.clubplatform.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp 요청에 인증 토큰을 추가하는 인터셉터
 */
class AuthInterceptor(private val context: Context? = null) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 인증 토큰 가져오기
        val token = getAuthToken()
        
        // 토큰이 있는 경우에만 헤더 추가
        val request = if (token != null) {
            Log.d("AuthInterceptor", "Adding auth token to request")
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            Log.d("AuthInterceptor", "No auth token available")
            originalRequest
        }
        
        return chain.proceed(request)
    }
    
    /**
     * SharedPreferences에서 인증 토큰을 가져오는 메서드
     */
    private fun getAuthToken(): String? {
        if (context == null) {
            Log.d("AuthInterceptor", "Context is null, cannot get auth token")
            return null
        }
        
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("access_token", null)
    }
}
