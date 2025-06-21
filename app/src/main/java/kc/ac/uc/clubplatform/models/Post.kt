package kc.ac.uc.clubplatform.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// 게시글 목록 응답
data class PostListResponse(
    val success: Boolean,
    val message: String,
    val posts: List<PostInfo>
)

// 게시글 정보 (목록용)
@Parcelize
data class PostInfo(
    val postId: Int,
    val title: String,
    val content: String,
    val authorName: String,
    val createdAt: String,
    val viewCount: Int,
    val commentCount: Int,
    val isNotice: Boolean = false
) : Parcelable

// 게시글 상세 응답
data class PostDetailResponse(
    val success: Boolean,
    val message: String,
    val post: PostDetail
)

// 게시글 상세 정보
@Parcelize
data class PostDetail(
    val postId: Int,
    val title: String,
    val content: String,
    val authorName: String,
    val createdAt: String,
    val updatedAt: String?,
    val viewCount: Int,
    val likeCount: Int,
    val isLiked: Boolean,
    val isScraped: Boolean,
    val isAnonymous: Boolean,
    val isNotice: Boolean? = null,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val commentCount: Int = 0
) : Parcelable

// 게시글 생성 요청
data class CreatePostRequest(
    val boardId: Int,
    val title: String,
    val content: String,
    val isAnonymous: Boolean,
    val isNotice: Boolean,
    val attachments: List<String>? = null
)

// 게시글 생성 응답
data class CreatePostResponse(
    val success: Boolean,
    val message: String,
    val postId: Int?,
    val createdAt: String?
)

// 게시글 수정 요청
data class UpdatePostRequest(
    val boardId: Int? = null,
    val title: String? = null,
    val content: String? = null,
    val isNotice: Boolean? = null,
    val attachments: List<String>? = null
)

// 게시글 수정 응답
data class UpdatePostResponse(
    val success: Boolean,
    val message: String,
    val updatedAt: String?
)

// 게시글 삭제 응답
data class DeletePostResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)

// 좋아요 응답
data class LikeResponse(
    val success: Boolean,
    val message: String,
    val isLiked: Boolean,
    val likeCount: Int
)

// 스크랩 응답
data class ScrapResponse(
    val success: Boolean,
    val message: String,
    val isScraped: Boolean
)

// BEST/HOT 게시판 응답
data class SpecialBoardResponse(
    val posts: List<PostInfo>,
    val totalCount: Int
)