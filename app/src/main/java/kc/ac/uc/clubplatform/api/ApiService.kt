package kc.ac.uc.clubplatform.api

import kc.ac.uc.clubplatform.models.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun registerUser(@Body userData: RegisterRequest): Response<RegisterResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<LogoutResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body refreshRequest: RefreshTokenRequest): Response<LoginResponse>

    // 백엔드 엔드포인트와 일치하도록 경로 수정
    @GET("auth/profile-image/{userId}")
    suspend fun getProfileImage(@Path("userId") userId: String): Response<ResponseBody>

    @GET("users")
    suspend fun testConnection(): Response<Any>

    // 비밀번호 변경 API
    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse>

    // 회원탈퇴 API
    @POST("auth/withdraw")
    suspend fun withdrawAccount(@Body request: WithdrawRequest): Response<ApiResponse>

    // 프로필 이미지 업로드 API (Base64 방식)
    @POST("auth/profile-image/update")
    suspend fun updateProfileImageBase64(@Body request: UpdateProfileImageBase64Request): Response<Map<String, Any>>

    // 학과정보 변경 API
    @POST("auth/update-department")
    suspend fun updateDepartment(@Body request: UpdateDepartmentRequest): Response<Map<String, Any>>

    // 동아리 관련 API 추가
    @GET("clubs/my")
    suspend fun getMyClubs(): Response<MyClubsResponse>

    @POST("clubs/join")
    suspend fun joinClub(@Body request: ClubJoinRequest): Response<ClubJoinResponse>

    @GET("clubs")
    suspend fun getClubList(): Response<ClubListResponse>

    // 게시판 관련 API 추가
    @GET("boards/club/{club_id}")
    suspend fun getBoardsByClub(@Path("club_id") clubId: Int): Response<BoardListResponse>

    @GET("posts/board/{board_id}")
    suspend fun getPostsByBoard(
        @Path("board_id") boardId: Int,
        @Query("boardType") boardType: String
    ): Response<PostListResponse>

    @GET("posts/{post_id}")
    suspend fun getPostDetail(@Path("post_id") postId: Int): Response<PostDetailResponse>

    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<CreatePostResponse>

    @PUT("posts/{postId}")
    suspend fun updatePost(
        @Path("postId") postId: Int,
        @Body request: UpdatePostRequest
    ): Response<UpdatePostResponse>

    @DELETE("posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: Int): Response<DeletePostResponse>

    @POST("posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: Int): Response<LikeResponse>

    @POST("posts/{postId}/scrap")
    suspend fun scrapPost(@Path("postId") postId: Int): Response<ScrapResponse>

    @GET("posts/best")
    suspend fun getBestPosts(): Response<SpecialBoardResponse>

    @GET("posts/hot")
    suspend fun getHotPosts(): Response<SpecialBoardResponse>
}


