// SearchActivity.kt
package kc.ac.uc.clubplatform.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kc.ac.uc.clubplatform.adapters.RecentSearchAdapter
import kc.ac.uc.clubplatform.adapters.SearchResultAdapter
import kc.ac.uc.clubplatform.databinding.ActivitySearchBinding
import kc.ac.uc.clubplatform.models.Post
import kc.ac.uc.clubplatform.models.RecentSearch

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    // 최근 검색어 목록 (실제로는 SharedPreferences나 DB에서 가져와야 함)
    private val recentSearches = mutableListOf<RecentSearch>()

    // 검색 결과 (실제로는 API 호출로 가져와야 함)
    private val searchResults = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 샘플 데이터 추가
        recentSearches.add(RecentSearch(1, "동아리"))
        recentSearches.add(RecentSearch(2, "모임"))
        recentSearches.add(RecentSearch(3, "프로젝트"))

        setupSearchBar()
        setupRecentSearches()
    }

    private fun setupSearchBar() {
        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 검색창에서 검색 버튼 클릭 시
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                    // 최근 검색어에 추가
                    addToRecentSearches(query)
                }
                return@setOnEditorActionListener true
            }
            false
        }

        // 검색 버튼 클릭 시
        binding.ivSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
                // 최근 검색어에 추가
                addToRecentSearches(query)
            }
        }
    }

    private fun setupRecentSearches() {
        if (recentSearches.isEmpty()) {
            binding.tvNoRecentSearches.visibility = View.VISIBLE
            binding.rvRecentSearches.visibility = View.GONE
            binding.btnClearRecentSearches.visibility = View.GONE
        } else {
            binding.tvNoRecentSearches.visibility = View.GONE
            binding.rvRecentSearches.visibility = View.VISIBLE
            binding.btnClearRecentSearches.visibility = View.VISIBLE

            val adapter = RecentSearchAdapter(
                recentSearches,
                onItemClick = { recentSearch ->
                    // 최근 검색어 클릭 시 해당 검색어로 검색
                    binding.etSearch.setText(recentSearch.query)
                    performSearch(recentSearch.query)
                },
                onDeleteClick = { recentSearch ->
                    // 최근 검색어 삭제
                    recentSearches.remove(recentSearch)
                    setupRecentSearches()
                }
            )

            binding.rvRecentSearches.layoutManager = LinearLayoutManager(this)
            binding.rvRecentSearches.adapter = adapter

            // 최근 검색어 전체 삭제 버튼
            binding.btnClearRecentSearches.setOnClickListener {
                recentSearches.clear()
                setupRecentSearches()
            }
        }
    }

    private fun addToRecentSearches(query: String) {
        // 이미 있는 검색어라면 제거
        recentSearches.removeAll { it.query == query }

        // 새로운 검색어 추가
        recentSearches.add(0, RecentSearch(recentSearches.size + 1, query))

        // 최근 검색어는 최대 10개만 유지
        if (recentSearches.size > 10) {
            recentSearches.removeAt(recentSearches.size - 1)
        }

        setupRecentSearches()
    }

    private fun performSearch(query: String) {
        // 실제 검색 수행 (API 호출 등)
        // 여기서는 샘플 데이터로 대체

        searchResults.clear()
        searchResults.add(Post(1, "5월 정기 모임 안내", "5월 10일 오후 6시부터...", "관리자", "2025-05-01", 15, 3))
        searchResults.add(Post(2, "춘계 MT 참가 신청", "이번 학기 춘계 MT...", "관리자", "2025-04-28", 32, 10))
        searchResults.add(Post(3, "새내기를 위한 대학 생활 꿀팁", "1. 수강신청은 미리 준비하세요...", "선배01", "2025-04-25", 45, 8))

        // 검색 결과가 있는 경우
        if (searchResults.isNotEmpty()) {
            binding.layoutRecentSearches.visibility = View.GONE
            binding.layoutSearchResults.visibility = View.VISIBLE

            val adapter = SearchResultAdapter(searchResults) { post ->
                // 검색 결과 클릭 이벤트 처리
                val intent = Intent(this, BoardActivity::class.java)
                intent.putExtra("post_id", post.id)
                startActivity(intent)
            }

            binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
            binding.rvSearchResults.adapter = adapter
        } else {
            // 검색 결과가 없는 경우
            binding.layoutRecentSearches.visibility = View.GONE
            binding.layoutSearchResults.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.GONE
            binding.tvNoSearchResults.visibility = View.VISIBLE
            binding.tvNoSearchResults.text = "'$query'에 대한 검색 결과가 없습니다."
        }
    }
}