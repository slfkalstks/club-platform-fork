package kc.ac.uc.clubplatform.models

data class Board(
    val id: Int,
    val name: String,
    val type: String,
    val description: String
)

// 게시판 목록 조회
data class BoardListResponse(
    val success: Boolean,
    val message: String,
    val boards: List<BoardInfo>
)

// 게시판 정보
data class BoardInfo(
    val boardId: Int,
    val type: String, // general, notice, hot, best, tips, my_posts, my_comments, my_scraps
    val name: String
)

// 특정 게시판 더 보기
data class BoardDetailResponse(
    val board: BoardInfo,
    val recentPosts: List<PostInfo>
)
