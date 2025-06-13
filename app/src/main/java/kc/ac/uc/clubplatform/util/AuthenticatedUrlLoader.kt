package kc.ac.uc.clubplatform.util

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import java.net.URL

/**
 * 인증 토큰을 포함한 이미지 URL 요청을 위한 유틸리티 클래스
 */
class AuthenticatedUrlLoader(
    private val context: Context,
    private val url: String
) {
    /**
     * 인증 토큰이 포함된 GlideUrl 객체 생성
     */
    fun getGlideUrl(): GlideUrl {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("access_token", null)
        
        return GlideUrl(
            url,
            object : Headers {
                override fun getHeaders(): Map<String, String> {
                    val headers = mutableMapOf<String, String>()
                    headers["Content-Type"] = "application/json"
                    if (accessToken != null) {
                        headers["Authorization"] = "Bearer $accessToken"
                    }
                    return headers
                }
            }
        )
    }
}
