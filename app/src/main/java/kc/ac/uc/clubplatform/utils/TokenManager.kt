package kc.ac.uc.clubplatform.utils

import android.content.Context
import android.util.Log

/**
 * 토큰 관리 유틸리티 클래스
 * 토큰 저장, 조회, 갱신, 삭제 등의 기능을 제공합니다.
 */
class TokenManager(private val context: Context) {
    private val TAG = "TokenManager"
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    
    // 토큰 저장
    fun saveTokens(accessToken: String, refreshToken: String, userId: String) {
        sharedPreferences.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putString("user_id", userId)
            putBoolean("is_logged_in", true)
            apply()
        }
        Log.d(TAG, "토큰 저장: userId=$userId")
    }
    
    // 액세스 토큰 조회
    fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }
    
    // 리프레시 토큰 조회
    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }
    
    // 사용자 ID 조회
    fun getUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }
    
    // 로그인 상태 확인
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
    
    // 토큰 정보 삭제 (로그아웃 시 사용)
    fun clearTokens() {
        sharedPreferences.edit().apply {
            remove("access_token")
            remove("refresh_token")
            remove("user_id")
            putBoolean("is_logged_in", false)
            apply()
        }
        Log.d(TAG, "토큰 정보 삭제 완료")
    }
    
    // JWT 토큰 만료 여부 확인 (필요시 구현)
    fun isTokenExpired(token: String): Boolean {
        // JWT 토큰 파싱 및 만료 시간 확인 로직 구현
        // 간단한 구현: Base64 디코딩 후 만료 시간 확인
        return false
    }
}