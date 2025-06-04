package kc.ac.uc.clubplatform.api

import com.google.gson.annotations.SerializedName

data class SchoolResponse(
    @SerializedName("dataSearch")
    val dataSearch: DataSearch? = null
)

data class DataSearch(
    @SerializedName("content")
    val content: Content? = null
)

data class Content(
    @SerializedName("row")
    val schools: List<School>? = null,
    
    @SerializedName("totalCount")
    val totalCount: Int = 0,
    
    @SerializedName("pageIndex")
    val pageIndex: Int = 0,
    
    @SerializedName("pageSize")
    val pageSize: Int = 0
)

data class School(
    @SerializedName("schoolName")
    val schoolName: String? = null,
    
    @SerializedName("schoolType")
    val schoolType: String? = null,
    
    @SerializedName("region")
    val region: String? = null,
    
    @SerializedName("schoolCode")
    val schoolCode: String? = null,
    
    @SerializedName("schoolAddr")
    val schoolAddr: String? = null,
    
    @SerializedName("estType")
    val establishmentType: String? = null
)
