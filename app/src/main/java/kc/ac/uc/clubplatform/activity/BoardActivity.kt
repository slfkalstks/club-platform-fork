// 업데이트된 BoardActivity.kt
package kc.ac.uc.clubplatform.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kc.ac.uc.clubplatform.databinding.ActivityBoardBinding
import kc.ac.uc.clubplatform.adapters.PostAdapter
import kc.ac.uc.clubplatform.adapters.CommentAdapter
import kc.ac.uc.clubplatform.api.ApiClient
import kc.ac.uc.clubplatform.models.PostInfo
import kc.ac.uc.clubplatform.models.PostDetail
import kotlinx.coroutines.launch
import io.noties.markwon.Markwon
import android.util.Log
import kc.ac.uc.clubplatform.models.CommentInfo
import kc.ac.uc.clubplatform.models.CreateCommentRequest
import java.text.SimpleDateFormat
import java.util.*
import android.widget.EditText
import androidx.core.content.ContextCompat
import kc.ac.uc.clubplatform.R
import kc.ac.uc.clubplatform.models.UpdateCommentRequest

class BoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardBinding
    private lateinit var boardType: String
    private lateinit var boardName: String
    private var postId: Int? = null
    private var boardId: Int? = null
    private var clubId: Int = -1
    private var anonymousCounter = 0 // 익명 번호 카운터
    private val anonymousMap = mutableMapOf<String, String>() // userId -> 익명번호 매핑
    private val comments = mutableListOf<CommentInfo>()
    private lateinit var commentsAdapter: CommentAdapter
    private val posts = mutableListOf<PostInfo>()
    private lateinit var postAdapter: PostAdapter
    private lateinit var markwon: Markwon
    private var currentPost: PostDetail? = null

    private val writePostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // 새 게시글이 작성되었으면 목록 새로고침 및 결과 전달
            loadPostList()
            setResult(RESULT_OK)
        }
    }

    private val editPostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // 게시글이 수정되었으면 상세 정보 새로고침 및 결과 전달
            postId?.let { loadPostDetail(it) }
            setResult(RESULT_OK)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 마크다운 초기화
        markwon = Markwon.create(this)

        // 인텐트에서 정보 가져오기
        boardType = intent.getStringExtra("board_type") ?: "general"
        boardName = intent.getStringExtra("board_name") ?: "게시판"
        postId = intent.getIntExtra("post_id", -1).takeIf { it != -1 }
        boardId = intent.getIntExtra("board_id", -1).takeIf { it != -1 }
        clubId = intent.getIntExtra("club_id", -1)

        // 현재 동아리 ID 가져오기
        if (clubId == -1) {
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            clubId = sharedPreferences.getInt("current_club_id", -1)
        }

        setupHeader()
        setupCommentAdapter()

        if (postId != null) {
            // 특정 게시글 화면 표시
            showPostDetail(postId!!)
        } else {
            // 게시판 목록 화면 표시
            showPostList()
        }
    }

    private fun setupHeader() {
        // 게시판 이름 설정
        binding.tvBoardName.text = boardName

        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 검색 버튼 숨기기 (요구사항에 따라)
        binding.ivSearch.visibility = View.GONE

        // 더보기 버튼 - 처음에는 숨김
        binding.ivMore.visibility = View.GONE
        binding.ivMore.setOnClickListener {
            showMoreMenu()
        }

        // 게시판 이름이 기본값인 경우 정보 로드
        if (boardName == "게시판") {
            lifecycleScope.launch {
                loadBoardInfoIfNeeded()
            }
        }
    }

    private fun setupCommentAdapter() {
        commentsAdapter = CommentAdapter(comments) { action, comment ->
            when (action) {
                "like" -> {
                    currentPost?.let { post ->
                        toggleCommentLike(post.postId, comment.commentId)
                    }
                }
                "edit" -> {
                    showEditCommentDialog(comment)
                }
                "delete" -> {
                    showDeleteCommentDialog(comment)
                }
                "reply" -> {
                    showReplyDialog(comment)
                }
            }
        }
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentsAdapter
    }

    private fun showEditCommentDialog(comment: CommentInfo) {
        val editText = EditText(this)
        editText.setText(comment.content)

        AlertDialog.Builder(this)
            .setTitle("댓글 수정")
            .setView(editText)
            .setPositiveButton("수정") { _, _ ->
                val newContent = editText.text.toString().trim()
                if (newContent.isNotEmpty()) {
                    currentPost?.let { post ->
                        updateComment(post.postId, comment.commentId, newContent)
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showDeleteCommentDialog(comment: CommentInfo) {
        AlertDialog.Builder(this)
            .setTitle("댓글 삭제")
            .setMessage("정말로 이 댓글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                currentPost?.let { post ->
                    deleteComment(post.postId, comment.commentId)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showReplyDialog(parentComment: CommentInfo) {
        val editText = EditText(this)
        editText.hint = "대댓글을 입력하세요"

        AlertDialog.Builder(this)
            .setTitle("대댓글 작성")
            .setView(editText)
            .setPositiveButton("작성") { _, _ ->
                val content = editText.text.toString().trim()
                if (content.isNotEmpty()) {
                    currentPost?.let { post ->
                        createReply(post.postId, content, parentComment.commentId)
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 대댓글 작성
    private fun createReply(postId: Int, content: String, parentId: Int) {
        lifecycleScope.launch {
            try {
                val request = CreateCommentRequest(
                    content = content,
                    isAnonymous = false, // 대댓글은 일단 익명 옵션 없이
                    parentId = parentId
                )

                val response = ApiClient.apiService.createComment(postId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    showToast("대댓글이 작성되었습니다")
                    loadComments(postId)
                    currentPost?.let { post ->
                        currentPost = post.copy(commentCount = post.commentCount + 1)
                        updatePostStats()
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "대댓글 작성에 실패했습니다"
                    showToast(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("BoardActivity", "Error creating reply", e)
                showToast("대댓글 작성 중 오류가 발생했습니다")
            }
        }
    }

    private fun showPostList() {
        // 더보기 버튼 숨기기
        binding.ivMore.visibility = View.GONE

        // 게시글 작성 버튼 표시
        binding.fabWritePost.show()
        binding.fabWritePost.setOnClickListener {
            val intent = Intent(this, WritePostActivity::class.java)
            intent.putExtra("board_type", boardType)
            intent.putExtra("board_name", boardName)
            intent.putExtra("board_id", boardId)
            intent.putExtra("club_id", clubId)
            writePostLauncher.launch(intent)
        }

        // 게시판 이름이 기본값인 경우 정보 로드
        lifecycleScope.launch {
            loadBoardInfoIfNeeded()
        }

        loadPostList()

        binding.rvPosts.visibility = View.VISIBLE
        binding.layoutPostDetail.visibility = View.GONE
    }

    private fun loadPostList() {
        lifecycleScope.launch {
            try {
                when (boardType) {
                    "best" -> loadBestPosts()
                    "hot" -> loadHotPosts()
                    else -> {
                        if (boardId != null) {
                            loadBoardPosts(boardId!!)
                        } else {
                            loadBoardsAndPosts()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BoardActivity", "Error loading posts", e)
                showToast("게시글을 불러오는 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }

    private suspend fun loadBoardsAndPosts() {
        try {
            val response = ApiClient.apiService.getBoardsByClub(clubId)
            if (response.isSuccessful && response.body()?.success == true) {
                val boards = response.body()?.boards ?: emptyList()
                val targetBoard = boards.find { it.type == boardType }

                if (targetBoard != null) {
                    boardId = targetBoard.boardId
                    boardName = targetBoard.name
                    binding.tvBoardName.text = boardName
                    loadBoardPosts(targetBoard.boardId)
                } else {
                    showToast("해당 게시판을 찾을 수 없습니다")
                }
            } else {
                showToast("게시판 정보를 불러올 수 없습니다")
            }
        } catch (e: Exception) {
            Log.e("BoardActivity", "Error loading boards", e)
            showToast("게시판을 불러오는 중 오류가 발생했습니다")
        }
    }

    private suspend fun loadBoardInfoIfNeeded() {
        // boardName이 기본값이고 boardId가 있는 경우 게시판 정보 조회
        if (boardName == "게시판" && boardId != null && boardId != -1) {
            try {
                val response = ApiClient.apiService.getBoardsByClub(clubId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val boards = response.body()?.boards ?: emptyList()
                    val targetBoard = boards.find { it.boardId == boardId }
                    if (targetBoard != null) {
                        boardName = targetBoard.name
                        binding.tvBoardName.text = boardName
                    }
                }
            } catch (e: Exception) {
                Log.e("BoardActivity", "Error loading board info", e)
            }
        }
        // boardType에 따른 기본 이름 설정
        else if (boardName == "게시판") {
            boardName = when (boardType) {
                "notice" -> "공지사항"
                "tips" -> "Tips"
                "hot" -> "HOT 게시판"
                "best" -> "BEST 게시판"
                "general" -> "자유게시판"
                else -> "게시판"
            }
            binding.tvBoardName.text = boardName
        }
    }

    private suspend fun loadBoardPosts(boardId: Int) {
        try {
            val response = ApiClient.apiService.getPostsByBoard(boardId, boardType)
            if (response.isSuccessful && response.body()?.success == true) {
                val postList = response.body()?.posts ?: emptyList()
                updatePostList(postList)
            } else {
                showToast("게시글을 불러올 수 없습니다")
            }
        } catch (e: Exception) {
            Log.e("BoardActivity", "Error loading board posts", e)
            showToast("게시글을 불러오는 중 오류가 발생했습니다")
        }
    }

    private suspend fun loadBestPosts() {
        try {
            val response = ApiClient.apiService.getBestPosts()
            if (response.isSuccessful) {
                val postList = response.body()?.posts ?: emptyList()
                updatePostList(postList)
            } else {
                showToast("BEST 게시글을 불러올 수 없습니다")
            }
        } catch (e: Exception) {
            Log.e("BoardActivity", "Error loading best posts", e)
            showToast("BEST 게시글을 불러오는 중 오류가 발생했습니다")
        }
    }

    private suspend fun loadHotPosts() {
        try {
            val response = ApiClient.apiService.getHotPosts()
            if (response.isSuccessful) {
                val postList = response.body()?.posts ?: emptyList()
                updatePostList(postList)
            } else {
                showToast("HOT 게시글을 불러올 수 없습니다")
            }
        } catch (e: Exception) {
            Log.e("BoardActivity", "Error loading hot posts", e)
            showToast("HOT 게시글을 불러오는 중 오류가 발생했습니다")
        }
    }

    private fun updatePostList(postList: List<PostInfo>) {
        posts.clear()
        posts.addAll(postList)

        postAdapter = PostAdapter(posts) { post ->
            // 게시글 클릭 시 상세 페이지로 이동
            val intent = Intent(this, BoardActivity::class.java)
            intent.putExtra("board_type", boardType)
            intent.putExtra("board_name", boardName)
            intent.putExtra("post_id", post.postId)
            intent.putExtra("board_id", boardId)
            intent.putExtra("club_id", clubId)
            startActivityForResult(intent, 1001)
        }

        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = postAdapter
    }

    private fun showPostDetail(postId: Int) {
        // 게시글 작성 버튼 숨기기
        binding.fabWritePost.hide()

        binding.rvPosts.visibility = View.GONE
        binding.layoutPostDetail.visibility = View.VISIBLE

        loadPostDetail(postId)
        setupPostDetailActions()

        // 게시판 이름이 기본값인 경우 정보 로드
        lifecycleScope.launch {
            loadBoardInfoIfNeeded()
        }
    }

    private fun loadPostDetail(postId: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getPostDetail(postId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val post = response.body()?.post
                    if (post != null) {
                        displayPostDetail(post)
                        currentPost = post
                        // 댓글 목록도 함께 로드
                        loadComments(postId)
                    }
                } else {
                    showToast("게시글을 불러올 수 없습니다")
                    finish()
                }
            } catch (e: Exception) {
                Log.e("BoardActivity", "Error loading post detail", e)
                showToast("게시글을 불러오는 중 오류가 발생했습니다")
                finish()
            }
        }
    }

    // 댓글 목록 조회
    private fun loadComments(postId: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getComments(postId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val commentList = response.body()?.comments ?: emptyList()
                    updateCommentList(commentList)
                } else {
                    Log.e("BoardActivity", "Failed to load comments: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                Log.e("BoardActivity", "Error loading comments", e)
            }
        }
    }

    // 댓글 목록 업데이트
    private fun updateCommentList(commentList: List<CommentInfo>) {
        // 익명 번호 재설정
        anonymousCounter = 0
        anonymousMap.clear()

        val processedComments = commentList.map { comment ->
            if (comment.isAnonymous) {
                val anonymousName = getAnonymousName(comment.authorName)
                comment.copy(authorName = anonymousName)
            } else {
                comment
            }
        }

        comments.clear()
        comments.addAll(processedComments)
        commentsAdapter.notifyDataSetChanged()
    }

    // 익명 이름 생성 - 같은 사용자는 같은 번호 유지
    private fun getAnonymousName(originalAuthor: String): String {
        return anonymousMap.getOrPut(originalAuthor) {
            anonymousCounter++
            "익명$anonymousCounter"
        }
    }

    // 댓글 작성
    private fun createComment(postId: Int, content: String) {
        lifecycleScope.launch {
            try {
                val isAnonymous = binding.cbAnonymous.isChecked // 익명 체크박스 값 읽기

                val request = CreateCommentRequest(
                    content = content,
                    isAnonymous = isAnonymous
                )

                val response = ApiClient.apiService.createComment(postId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    binding.etComment.setText("")
                    binding.cbAnonymous.isChecked = false // 체크박스 초기화
                    showToast("댓글이 작성되었습니다")
                    // 댓글 목록 새로고침
                    loadComments(postId)
                    // 게시글의 댓글 수 업데이트
                    currentPost?.let { post ->
                        currentPost = post.copy(commentCount = post.commentCount + 1)
                        updatePostStats()
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "댓글 작성에 실패했습니다"
                    showToast(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("BoardActivity", "Error creating comment", e)
                showToast("댓글 작성 중 오류가 발생했습니다")
            }
        }
    }

    // 댓글 수정
    private fun updateComment(postId: Int, commentId: Int, content: String) {
        lifecycleScope.launch {
            try {
                val request = UpdateCommentRequest(content = content)
                val response = ApiClient.apiService.updateComment(postId, commentId, request)

                if (response.isSuccessful && response.body()?.success == true) {
                    showToast("댓글이 수정되었습니다")
                    loadComments(postId)
                } else {
                    val errorMessage = response.body()?.message ?: "댓글 수정에 실패했습니다"
                    showToast(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("BoardActivity", "Error updating comment", e)
                showToast("댓글 수정 중 오류가 발생했습니다")
            }
        }
    }

    // 댓글 삭제
    private fun deleteComment(postId: Int, commentId: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.deleteComment(postId, commentId)
                if (response.isSuccessful && response.body()?.success == true) {
                    showToast("댓글이 삭제되었습니다")
                    loadComments(postId)
                    // 게시글의 댓글 수 업데이트
                    currentPost?.let { post ->
                        currentPost = post.copy(commentCount = post.commentCount - 1)
                        updatePostStats()
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "댓글 삭제에 실패했습니다"
                    showToast(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("BoardActivity", "Error deleting comment", e)
                showToast("댓글 삭제 중 오류가 발생했습니다")
            }
        }
    }

    // 댓글 좋아요
    private fun toggleCommentLike(postId: Int, commentId: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.likeComment(postId, commentId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val likeResponse = response.body()!!
                    // 댓글 목록에서 해당 댓글 찾아서 좋아요 상태 업데이트
                    val commentIndex = comments.indexOfFirst { it.commentId == commentId }
                    if (commentIndex != -1) {
                        comments[commentIndex] = comments[commentIndex].copy(
                            isLiked = likeResponse.isLiked,
                            likeCount = likeResponse.likeCount
                        )
                        commentsAdapter.notifyItemChanged(commentIndex)
                    }
                } else {
                    showToast("댓글 좋아요 처리 중 오류가 발생했습니다")
                }
            } catch (e: Exception) {
                Log.e("BoardActivity", "Error toggling comment like", e)
                showToast("댓글 좋아요 처리 중 오류가 발생했습니다")
            }
        }
    }

    // 게시글 통계 정보 업데이트 (조회수, 댓글수, 스크랩수)
    private fun updatePostStats() {
        currentPost?.let { post ->
            binding.tvPostViewCount.text = post.viewCount.toString()
            binding.tvPostCommentCount.text = post.commentCount.toString()
        }
    }

    private fun displayPostDetail(post: PostDetail) {
        binding.tvPostTitle.text = post.title
        binding.tvPostAuthor.text = if (post.isAnonymous) "익명" else post.authorName
        binding.tvPostDate.text = formatDate(post.createdAt)

        // 마크다운 렌더링
        markwon.setMarkdown(binding.tvPostContent, post.content)

        // 통계 정보 업데이트
        updatePostStats()

        // 좋아요/스크랩 버튼 상태 업데이트
        updateLikeButton(post.isLiked, post.likeCount)
        updateScrapButton(post.isScraped)

        // 수정/삭제 권한에 따른 메뉴 표시
        binding.ivMore.visibility = if (post.canEdit || post.canDelete) View.VISIBLE else View.GONE
    }

    private fun showMoreMenu() {
        val post = currentPost ?: return

        val popupMenu = PopupMenu(this, binding.ivMore)

        if (post.canEdit) {
            popupMenu.menu.add(0, 1, 0, "수정")
        }
        if (post.canDelete) {
            popupMenu.menu.add(0, 2, 0, "삭제")
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    // 수정
                    editPost()
                    true
                }
                2 -> {
                    // 삭제
                    showDeleteConfirmDialog()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun editPost() {
        val post = currentPost ?: return

        val intent = Intent(this, WritePostActivity::class.java)
        intent.putExtra("board_type", boardType)
        intent.putExtra("board_name", boardName)
        intent.putExtra("board_id", boardId)
        intent.putExtra("club_id", clubId)
        intent.putExtra("post_id", post.postId)
        intent.putExtra("title", post.title)
        intent.putExtra("content", post.content)
        intent.putExtra("is_anonymous", post.isAnonymous)
        intent.putExtra("is_notice", post.isNotice ?: false)
        intent.putExtra("is_edit_mode", true)
        editPostLauncher.launch(intent)
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("게시글 삭제")
            .setMessage("정말로 이 게시글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deletePost()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deletePost() {
        val post = currentPost ?: return

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.deletePost(post.postId)
                if (response.isSuccessful && response.body()?.success == true) {
                    showToast("게시글이 삭제되었습니다")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorMessage = response.body()?.message ?: "게시글 삭제에 실패했습니다"
                    showToast(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("BoardActivity", "Error deleting post", e)
                showToast("게시글 삭제 중 오류가 발생했습니다")
            }
        }
    }

    private fun setupPostDetailActions() {
        // 좋아요 버튼
        binding.btnLike.setOnClickListener {
            currentPost?.let { post ->
                toggleLike(post.postId)
            }
        }

        // 스크랩 버튼
        binding.btnScrap.setOnClickListener {
            currentPost?.let { post ->
                toggleScrap(post.postId)
            }
        }

        // 댓글 전송 버튼 - 실제 API 구현으로 교체
        binding.btnSendComment.setOnClickListener {
            val commentText = binding.etComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                currentPost?.let { post ->
                    createComment(post.postId, commentText)
                }
            }
        }
    }

    private fun toggleLike(postId: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.likePost(postId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val likeResponse = response.body()!!
                    updateLikeButton(likeResponse.isLiked, likeResponse.likeCount)

                    // 현재 게시글 정보 업데이트
                    currentPost = currentPost?.copy(
                        isLiked = likeResponse.isLiked,
                        likeCount = likeResponse.likeCount
                    )
                } else {
                    showToast("좋아요 처리 중 오류가 발생했습니다")
                }
            } catch (e: Exception) {
                Log.e("BoardActivity", "Error toggling like", e)
                showToast("좋아요 처리 중 오류가 발생했습니다")
            }
        }
    }

    private fun toggleScrap(postId: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.scrapPost(postId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val scrapResponse = response.body()!!
                    updateScrapButton(scrapResponse.isScraped)

                    // 현재 게시글 정보 업데이트
                    currentPost = currentPost?.copy(isScraped = scrapResponse.isScraped)

                    showToast(if (scrapResponse.isScraped) "스크랩했습니다" else "스크랩을 취소했습니다")
                } else {
                    showToast("스크랩 처리 중 오류가 발생했습니다")
                }
            } catch (e: Exception) {
                Log.e("BoardActivity", "Error toggling scrap", e)
                showToast("스크랩 처리 중 오류가 발생했습니다")
            }
        }
    }

    // 좋아요 버튼 상태 업데이트
    private fun updateLikeButton(isLiked: Boolean, likeCount: Int) {
        if (isLiked) {
            binding.btnLike.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_filled, 0, 0, 0)
        } else {
            binding.btnLike.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray))
            binding.btnLike.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
            binding.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border, 0, 0, 0)
        }
        binding.btnLike.text = "공감 $likeCount"
    }

    // 스크랩 버튼 상태 업데이트
    private fun updateScrapButton(isScraped: Boolean) {
        if (isScraped) {
            binding.btnScrap.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))
            binding.btnScrap.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.btnScrap.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bookmark_filled, 0, 0, 0)
            binding.btnScrap.text = "스크랩"
        } else {
            binding.btnScrap.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray))
            binding.btnScrap.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
            binding.btnScrap.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bookmark_border, 0, 0, 0)
            binding.btnScrap.text = "스크랩"
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // 하위 액티비티에서 변경사항이 있었으면 목록 새로고침
            loadPostList()
            setResult(RESULT_OK)
        }
    }
}