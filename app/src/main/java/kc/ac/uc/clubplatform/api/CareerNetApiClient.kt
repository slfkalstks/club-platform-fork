package kc.ac.uc.clubplatform.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class CareerNetApiClient(private val apiKey: String) {
    private val client = OkHttpClient()
    private val baseUrl = "https://www.career.go.kr/cnet/openapi/getOpenApi"
    
    // 대학교 검색 메소드
    suspend fun searchUniversity(universityName: String): JSONObject {
        val encodedName = URLEncoder.encode(universityName, StandardCharsets.UTF_8.toString())
        val url = "$baseUrl?apiKey=$apiKey&svcType=api&svcCode=SCHOOL&contentType=json&gubun=univ_list&searchSchulNm=$encodedName"
        
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("응답이 없습니다")
            
            JSONObject(responseBody)
        }
    }
    
    // 대학교 상세 정보 조회 메소드
    suspend fun getUniversityDetails(schoolCode: String): JSONObject {
        val url = "$baseUrl?apiKey=$apiKey&svcType=api&svcCode=SCHOOL&contentType=json&gubun=univ_detail&schoolCode=$schoolCode"
        
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("응답이 없습니다")
            
            JSONObject(responseBody)
        }
    }
    
    // 학과 정보 조회 메소드
    suspend fun getDepartmentInfo(schoolCode: String): JSONObject {
        val url = "$baseUrl?apiKey=$apiKey&svcType=api&svcCode=MAJOR&contentType=json&gubun=univ_list&thisPage=1&perPage=100&schoolCode=$schoolCode"
        
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("응답이 없습니다")
            
            JSONObject(responseBody)
        }
    }
    
    // 학과 상세 정보 조회 메소드
    suspend fun getDepartmentDetails(majorSeq: String): JSONObject {
        val url = "$baseUrl?apiKey=$apiKey&svcType=api&svcCode=MAJOR&contentType=json&gubun=univ_detail&majorSeq=$majorSeq"
        
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("응답이 없습니다")
            
            JSONObject(responseBody)
        }
    }
}
