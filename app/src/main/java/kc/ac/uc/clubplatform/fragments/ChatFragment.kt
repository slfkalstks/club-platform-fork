// fragments/ChatFragment.kt
package kc.ac.uc.clubplatform.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kc.ac.uc.clubplatform.databinding.FragmentChatBinding
import kc.ac.uc.clubplatform.adapters.ChatRoomAdapter
import kc.ac.uc.clubplatform.models.ChatRoom

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    // 채팅방 목록 (실제로는 DB에서 불러와야 함)
    private val chatRooms = mutableListOf<ChatRoom>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 샘플 데이터 추가
        chatRooms.add(ChatRoom(1, "김철수", "안녕하세요! 다음 모임 시간이 어떻게 되나요?", "오후 2:30", 1))
        chatRooms.add(ChatRoom(2, "동아리 운영진", "다음 주 정기 모임 공지드립니다.", "오전 10:15", 3))
        chatRooms.add(ChatRoom(3, "프로젝트 팀", "발표 자료 완성했습니다. 확인해주세요.", "어제", 0))

        setupRecyclerView()
        setupButtons()
    }

    private fun setupRecyclerView() {
        val adapter = ChatRoomAdapter(chatRooms) { chatRoom ->
            // 채팅방 클릭 이벤트 처리 (실제 구현 생략)
        }

        binding.rvChatRooms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatRooms.adapter = adapter
    }

    private fun setupButtons() {
        // 새 채팅 버튼
        binding.btnNewChat.setOnClickListener {
            // 새 채팅방 생성 다이얼로그 표시 (실제 구현 생략)
        }

        // 채팅 삭제 버튼
        binding.btnDeleteChat.setOnClickListener {
            // 채팅 삭제 모드 활성화 (실제 구현 생략)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}