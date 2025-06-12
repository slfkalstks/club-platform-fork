package kc.ac.uc.clubplatform.api

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

    //학과 정보 조회
    suspend fun getDepartmentInfo(schoolCode: String): JSONObject {
        val url = "$OPENAPI_URL?apiKey=$API_KEY&svcType=api&svcCode=MAJOR&contentType=json&gubun=univ_list&thisPage=1&perPage=100&schoolCode=$schoolCode"
        return getJsonByOkHttp(url)
    }

    //학과 정보 상세 조회
    suspend fun getDepartmentDetails(majorSeq: String): JSONObject {
        val url = "$OPENAPI_URL?apiKey=$API_KEY&svcType=api&svcCode=MAJOR&contentType=json&gubun=univ_detail&majorSeq=$majorSeq"
        return getJsonByOkHttp(url)
    }

    private suspend fun getJsonByOkHttp(url: String): JSONObject {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("응답이 없습니다")
            JSONObject(responseBody)
        }
    }
}
