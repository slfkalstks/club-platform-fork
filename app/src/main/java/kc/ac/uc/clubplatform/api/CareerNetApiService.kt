package kc.ac.uc.clubplatform.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CareerNetApiService {
    @GET("openapi/OpenAPI/getOpenApi")
    fun searchSchools(
        @Query("apiKey") apiKey: String,
        @Query("svcType") serviceType: String = "api",
        @Query("svcCode") serviceCode: String = "SCHOOL",
        @Query("contentType") contentType: String = "json", // XML 대신 JSON 요청
        @Query("gubun") schoolType: String = "univ", // 대학교만 검색하도록 기본값 지정
        @Query("searchSchulNm") schoolName: String
    ): Call<SchoolResponse>
}
