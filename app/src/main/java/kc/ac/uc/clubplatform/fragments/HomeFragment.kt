// fragments/HomeFragment.kt
package kc.ac.uc.clubplatform.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kc.ac.uc.clubplatform.databinding.FragmentHomeBinding
import kc.ac.uc.clubplatform.activity.BoardActivity
import kc.ac.uc.clubplatform.activity.NotificationActivity
import kc.ac.uc.clubplatform.activity.ProfileActivity
import kc.ac.uc.clubplatform.activity.SearchActivity
import kc.ac.uc.clubplatform.activity.ClubJoinActivity
import kc.ac.uc.clubplatform.adapters.NoticeAdapter
import kc.ac.uc.clubplatform.adapters.TipAdapter
import kc.ac.uc.clubplatform.adapters.ClubListAdapter
import kc.ac.uc.clubplatform.models.Post
import kc.ac.uc.clubplatform.models.Club
import kc.ac.uc.clubplatform.api.ApiClient
import kc.ac.uc.clubplatform.databinding.DialogClubListBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        setupBoardButtons()
        loadCurrentClubData()
    }

    private fun setupHeader() {
        // 검색 아이콘 클릭 시 검색 페이지로 이동
        binding.ivSearch.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }

        // 알림 아이콘 클릭 시 알림 페이지로 이동
        binding.ivNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }

        // 프로필 아이콘 클릭 시 프로필 페이지로 이동
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        // 동아리명 클릭 시 동아리 목록 다이얼로그 표시
        binding.tvClubName.setOnClickListener {
            showClubListDialog()
        }
    }

    private fun loadCurrentClubData() {
        // SharedPreferences에서 현재 동아리 정보 로드
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val currentClubId = sharedPreferences.getInt("current_club_id", -1)
        val currentClubName = sharedPreferences.getString("current_club_name", "")

        if (currentClubId != -1 && !currentClubName.isNullOrEmpty()) {
            // 저장된 동아리 정보가 있으면 바로 표시
            binding.tvClubName.text = currentClubName
            setupNoticesRecyclerView(currentClubId)
            setupTipsRecyclerView(currentClubId)
        } else {
            // 저장된 정보가 없으면 API로 동아리 목록 조회
            loadMyClubsAndSetCurrent()
        }
    }

    private fun loadMyClubsAndSetCurrent() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getMyClubs()

                if (response.isSuccessful && response.body()?.success == true) {
                    val clubs = response.body()?.data ?: emptyList()

                    if (clubs.isNotEmpty()) {
                        // 저장된 동아리 정보 다시 확인
                        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                        val savedClubId = sharedPreferences.getInt("current_club_id", -1)
                        val savedClubName = sharedPreferences.getString("current_club_name", "")

                        // 저장된 동아리가 현재 가입된 동아리 목록에 있는지 확인
                        val savedClub = clubs.find { it.clubId == savedClubId }

                        val currentClub = if (savedClub != null && !savedClubName.isNullOrEmpty()) {
                            // 저장된 동아리가 유효하면 그대로 사용 (기존 사용자)
                            savedClub
                        } else {
                            // 저장된 동아리가 없거나 유효하지 않으면 첫 번째 동아리 설정
                            val firstClub = clubs.first()
                            saveCurrentClub(firstClub.clubId, firstClub.name)
                            firstClub
                        }

                        binding.tvClubName.text = currentClub.name
                        setupNoticesRecyclerView(currentClub.clubId)
                        setupTipsRecyclerView(currentClub.clubId)
                    } else {
                        // 가입된 동아리가 없는 경우
                        binding.tvClubName.text = "동아리 없음"
                        setupNoticesRecyclerView(-1)
                        setupTipsRecyclerView(-1)
                    }
                } else {
                    // API 호출 실패 시 기본값 설정
                    binding.tvClubName.text = "동아리 정보 없음"
                    setupNoticesRecyclerView(-1)
                    setupTipsRecyclerView(-1)
                }
            } catch (e: Exception) {
                // 네트워크 오류 시 기본값 설정
                binding.tvClubName.text = "네트워크 오류"
                setupNoticesRecyclerView(-1)
                setupTipsRecyclerView(-1)
            }
        }
    }

    private fun showClubListDialog() {
        val dialog = Dialog(requireContext())
        val dialogBinding = DialogClubListBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // 다이얼로그 크기 설정
        val window = dialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // 동아리 목록 로드
        loadMyClubs(dialogBinding, dialog)

        // 닫기 버튼
        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // 동아리 추가 버튼
        dialogBinding.btnAddClub.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(requireContext(), ClubJoinActivity::class.java)
            startActivity(intent)
        }

        dialog.show()
    }

    private fun loadMyClubs(dialogBinding: DialogClubListBinding, dialog: Dialog) {
        lifecycleScope.launch {
            try {
                dialogBinding.progressBar.visibility = View.VISIBLE

                val response = ApiClient.apiService.getMyClubs()

                if (response.isSuccessful && response.body()?.success == true) {
                    val clubs = response.body()?.data ?: emptyList()

                    if (clubs.isNotEmpty()) {
                        setupClubRecyclerView(dialogBinding, clubs, dialog)
                        dialogBinding.tvNoClubs.visibility = View.GONE
                        dialogBinding.rvClubs.visibility = View.VISIBLE
                    } else {
                        dialogBinding.tvNoClubs.visibility = View.VISIBLE
                        dialogBinding.rvClubs.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(requireContext(), "동아리 목록을 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                dialogBinding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupClubRecyclerView(dialogBinding: DialogClubListBinding, clubs: List<Club>, dialog: Dialog) {
        val adapter = ClubListAdapter(clubs) { selectedClub ->
            // 동아리 선택 시 현재 동아리로 설정
            saveCurrentClub(selectedClub.clubId, selectedClub.name)

            // UI 즉시 업데이트
            binding.tvClubName.text = selectedClub.name
            setupNoticesRecyclerView(selectedClub.clubId)
            setupTipsRecyclerView(selectedClub.clubId)

            Toast.makeText(requireContext(), "${selectedClub.name}(으)로 전환되었습니다", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialogBinding.rvClubs.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.rvClubs.adapter = adapter
    }

    private fun saveCurrentClub(clubId: Int, clubName: String) {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt("current_club_id", clubId)
            .putString("current_club_name", clubName)
            .apply()
    }

    private fun setupNoticesRecyclerView(clubId: Int) {
        // 현재 동아리에 맞는 공지사항 데이터 (나중에 실제 API로 교체)
        val notices = if (clubId > 0) {
            getNoticesForClub(clubId)
        } else {
            emptyList()
        }

        val adapter = NoticeAdapter(notices) { post ->
            // 공지사항 클릭 이벤트 처리
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", "notice")
            intent.putExtra("post_id", post.id)
            intent.putExtra("club_id", clubId)
            startActivity(intent)
        }

        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotices.adapter = adapter

        // 공지사항 더보기 버튼 클릭 이벤트
        binding.btnMoreNotices.setOnClickListener {
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", "notice")
            intent.putExtra("club_id", clubId)
            startActivity(intent)
        }
    }

    private fun setupTipsRecyclerView(clubId: Int) {
        // 현재 동아리에 맞는 팁 데이터 (나중에 실제 API로 교체)
        val tips = if (clubId > 0) {
            getTipsForClub(clubId)
        } else {
            emptyList()
        }

        val adapter = TipAdapter(tips) { post ->
            // 팁 클릭 이벤트 처리
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", "tips")
            intent.putExtra("post_id", post.id)
            intent.putExtra("club_id", clubId)
            startActivity(intent)
        }

        binding.rvTips.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTips.adapter = adapter

        // 팁 더보기 버튼 클릭 이벤트
        binding.btnMoreTips.setOnClickListener {
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", "tips")
            intent.putExtra("club_id", clubId)
            startActivity(intent)
        }
    }

    // 동아리별 공지사항 데이터 (임시 - 나중에 실제 API로 교체)
    private fun getNoticesForClub(clubId: Int): List<Post> {
        return listOf(
            Post(1, "동아리 정기 모임 안내", "이번 주 정기 모임은 금요일 오후 6시에...", "관리자", "2025-06-18", 15, 3),
            Post(2, "동아리 활동 관련 공지", "다음 주부터 새로운 프로젝트를 시작합니다...", "관리자", "2025-06-15", 32, 10)
        )
    }

    // 동아리별 팁 데이터 (임시 - 나중에 실제 API로 교체)
    private fun getTipsForClub(clubId: Int): List<Post> {
        return listOf(
            Post(3, "동아리 활동 꿀팁", "동아리 활동을 할 때 이것만은 꼭...", "선배01", "2025-06-10", 45, 8),
            Post(4, "새내기를 위한 가이드", "동아리에 처음 가입한 새내기들을 위한...", "회장", "2025-06-08", 38, 12)
        )
    }

    private fun setupBoardButtons() {
        // 현재 동아리 ID 가져오기
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val currentClubId = sharedPreferences.getInt("current_club_id", -1)

        // 일반 게시판 버튼 클릭 이벤트
        binding.btnGeneralBoard.setOnClickListener {
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", "general")
            intent.putExtra("club_id", currentClubId)
            startActivity(intent)
        }

        // 공지 게시판 버튼 클릭 이벤트
        binding.btnNoticeBoard.setOnClickListener {
            val intent = Intent(requireContext(), BoardActivity::class.java)
            intent.putExtra("board_type", "notice")
            intent.putExtra("club_id", currentClubId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // 다른 화면에서 돌아왔을 때 동아리 정보가 변경되었을 수 있으므로 다시 로드
        refreshCurrentClubData()
    }

    private fun refreshCurrentClubData() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val currentClubId = sharedPreferences.getInt("current_club_id", -1)
        val currentClubName = sharedPreferences.getString("current_club_name", "")

        if (currentClubId != -1 && !currentClubName.isNullOrEmpty()) {
            binding.tvClubName.text = currentClubName
            // 데이터 새로고침은 필요시에만 (성능 고려)
            if (binding.rvNotices.adapter == null) {
                setupNoticesRecyclerView(currentClubId)
                setupTipsRecyclerView(currentClubId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}