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
import kc.ac.uc.clubplatform.models.BoardInfo
import kotlinx.coroutines.launch
import io.noties.markwon.Markwon
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class BoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardBinding
    private lateinit var boardType: String
    private lateinit var boardName: String
    private var postId: Int? = null
    private var boardId: Int? = null
    private var clubId: Int = -1
    private val comments = mutableListOf<String>()
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
        commentsAdapter = CommentAdapter(comments)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentsAdapter
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

    private fun displayPostDetail(post: PostDetail) {
        binding.tvPostTitle.text = post.title
        binding.tvPostAuthor.text = if (post.isAnonymous) "익명" else post.authorName
        binding.tvPostDate.text = formatDate(post.createdAt)

        // 마크다운 렌더링
        markwon.setMarkdown(binding.tvPostContent, post.content)

        binding.tvPostViewCount.text = post.viewCount.toString()
        binding.tvPostCommentCount.text = post.commentCount.toString()

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

        // 댓글 전송 버튼
        binding.btnSendComment.setOnClickListener {
            val commentText = binding.etComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                // 임시로 로컬에 추가 (실제로는 API 호출 필요)
                comments.add(commentText)
                commentsAdapter.notifyItemInserted(comments.size - 1)
                binding.rvComments.scrollToPosition(comments.size - 1)
                binding.etComment.setText("")

                // TODO: 실제 댓글 API 구현
                showToast("댓글 API는 추후 구현 예정입니다")
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

    private fun updateLikeButton(isLiked: Boolean, likeCount: Int) {
        binding.btnLike.text = "공감 $likeCount"
        binding.btnLike.isSelected = isLiked
        // 선택된 상태에 따른 색상 변경은 selector로 처리
    }

    private fun updateScrapButton(isScraped: Boolean) {
        binding.btnScrap.text = if (isScraped) "스크랩됨" else "스크랩"
        binding.btnScrap.isSelected = isScraped
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