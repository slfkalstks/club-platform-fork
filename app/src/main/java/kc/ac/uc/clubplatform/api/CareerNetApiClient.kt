package kc.ac.uc.clubplatform.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kc.ac.uc.clubplatform.BuildConfig

object CareerNetApiClient {
    private val BASE_URL = BuildConfig.CAREER_NET_API_URL
    private val OPENAPI_URL = BuildConfig.CAREER_NET_OPENAPI_URL
    val API_KEY = BuildConfig.CAREER_NET_API_KEY

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val schoolApiService: CareerNetApiService = retrofit.create(CareerNetApiService::class.java)

    //대학 정보 조회
    suspend fun searchUniversity(universityName: String): JSONObject {
        val encodedName = URLEncoder.encode(universityName, StandardCharsets.UTF_8.toString())
        val url = "$OPENAPI_URL?apiKey=$API_KEY&svcType=api&svcCode=SCHOOL&contentType=json&gubun=univ_list&searchSchulNm=$encodedName"
        return getJsonByOkHttp(url)
    }

    //대학 정보 상세 조회
    suspend fun getUniversityDetails(schoolCode: String): JSONObject {
        val url = "$OPENAPI_URL?apiKey=$API_KEY&svcType=api&svcCode=SCHOOL&contentType=json&gubun=univ_detail&schoolCode=$schoolCode"
        return getJsonByOkHttp(url)
    }

    // 학과 정보 조회 (학과명으로 검색) - searchTitle 파라미터 사용으로 수정
    suspend fun searchMajorByName(majorName: String, page: Int = 1, perPage: Int = 100): JSONObject {
        val encodedName = URLEncoder.encode(majorName, StandardCharsets.UTF_8.toString())
        val url = "$OPENAPI_URL?apiKey=$API_KEY&svcType=api&svcCode=MAJOR&contentType=json&gubun=univ_list&thisPage=$page&perPage=$perPage&searchTitle=$encodedName"
        Log.d("CareerNetApiClient", "Searching major by name: $majorName, page: $page, perPage: $perPage")
        return getJsonByOkHttp(url)
    }

    // 학과 정보 전체 목록 조회
    suspend fun getAllMajors(page: Int = 1, perPage: Int = 100): JSONObject {
        val url = "$OPENAPI_URL?apiKey=$API_KEY&svcType=api&svcCode=MAJOR&contentType=json&gubun=univ_list&thisPage=$page&perPage=$perPage"
        Log.d("CareerNetApiClient", "Getting all majors, page: $page, perPage: $perPage")
        return getJsonByOkHttp(url)
    }

    //학과 정보 조회 (학교코드 기준)
    suspend fun getDepartmentInfo(schoolCode: String): JSONObject {
        val url = "$OPENAPI_URL?apiKey=$API_KEY&svcType=api&svcCode=MAJOR&contentType=json&gubun=univ_list&thisPage=1&perPage=100&schoolCode=$schoolCode"
        Log.d("CareerNetApiClient", "Getting department info with URL: $url")
        return getJsonByOkHttp(url)
    }

    //학과 정보 상세 조회
    suspend fun getDepartmentDetails(majorSeq: String): JSONObject {
        val url = "$OPENAPI_URL?apiKey=$API_KEY&svcType=api&svcCode=MAJOR&contentType=json&gubun=univ_detail&majorSeq=$majorSeq"
        return getJsonByOkHttp(url)
    }

    private suspend fun getJsonByOkHttp(url: String): JSONObject {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).get().build()
                Log.d("CareerNetApiClient", "Sending request to: ${url.replace(API_KEY, "API_KEY_HIDDEN")}")
                
                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                    Log.d("CareerNetApiClient", "Response received, status: ${response.code}")
                    JSONObject(responseBody)
                } else {
                    Log.e("CareerNetApiClient", "API call failed, status: ${response.code}, message: ${response.message}")
                    if (!responseBody.isNullOrEmpty()) {
                        Log.e("CareerNetApiClient", "Response body: $responseBody")
                    }
                    throw Exception("API 호출 실패: ${response.code} ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("CareerNetApiClient", "Exception during API call", e)
                throw Exception("API 요청 중 오류 발생: ${e.message}")
            }
        }
    }
}