// fragments/NewsFeedFragment.kt
package kc.ac.uc.clubplatform.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kc.ac.uc.clubplatform.adapters.NotificationAdapter
import kc.ac.uc.clubplatform.databinding.FragmentNewsFeedBinding
import kc.ac.uc.clubplatform.models.Notification

class NewsFeedFragment : Fragment() {
    private var _binding: FragmentNewsFeedBinding? = null
    private val binding get() = _binding!!

    private val notifications = mutableListOf<Notification>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 샘플 데이터 추가
        notifications.add(Notification(1, "공지사항", "5월 정기 모임 안내가 등록되었습니다.", "2025-05-05", false))
        notifications.add(Notification(2, "댓글", "홍길동님이 내 게시글에 댓글을 남겼습니다.", "2025-05-04", true))
        notifications.add(Notification(3, "일정", "내일 동아리 정기 모임이 있습니다.", "2025-05-03", false))

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = NotificationAdapter(notifications)
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}