package kc.ac.uc.clubplatform.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Club(
    val clubId: Int,
    val name: String,
    val description: String?,
    val category: Category?,
    val organization: String?,
    val logoImage: String?,
    val meetingPlace: String?,
    val meetingTime: String?,
    val fee: Int?,
    val joinMethod: String?,
    val visibility: String?,
    val createdBy: Int?,
    val createdAt: String?,
    val updatedAt: String?,
    val memberCount: Int?,
    val myRole: String?,
    val isJoined: Boolean?
) : Parcelable

@Parcelize
data class Category(
    val categoryId: Int,
    val name: String
) : Parcelable

// API 응답 모델들
data class MyClubsResponse(
    val success: Boolean,
    val message: String,
    val data: List<Club>
)

data class ClubJoinResponse(
    val success: Boolean,
    val message: String,
    val clubId: Int?,
    val role: String?
)

data class ClubListResponse(
    val success: Boolean,
    val message: String,
    val clubs: List<Club>,
    val totalCount: Int
)

data class ClubJoinRequest(
    val inviteCode: String
)