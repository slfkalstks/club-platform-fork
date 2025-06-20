package kc.ac.uc.clubplatform.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val date: String,
    val viewCount: Int,
    val commentCount: Int,
    val isNotice: Boolean = false
) : Parcelable

// 게시글 목록 조회 응답
data class PostListResponse(
    val success: Boolean,
    val message: String,
    val posts: List<PostInfo>
)

data class PostInfo(
    val postId: Int,
    val title: String,
    val content: String,
    val authorName: String,
    val createdAt: String,
    val viewCount: Int,
    val commentCount: Int
)

// 게시글 상세 조회 응답
data class PostDetailResponse(
    val success: Boolean,
    val message: String,
    val post: PostDetail
)

data class PostDetail(
    val postId: Int,
    val title: String,
    val content: String, // 마크다운
    val authorName: String,
    val createdAt: String,
    val updatedAt: String,
    val viewCount: Int,
    val likeCount: Int,
    val isLiked: Boolean,
    val isScraped: Boolean,
    val isAnonymous: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean
)
