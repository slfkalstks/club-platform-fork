package kc.ac.uc.clubplatform.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kc.ac.uc.clubplatform.R
import kc.ac.uc.clubplatform.databinding.FragmentChatBinding
import kc.ac.uc.clubplatform.databinding.DialogNewChatBinding
import kc.ac.uc.clubplatform.databinding.DialogDeleteChatBinding
import kc.ac.uc.clubplatform.adapters.ChatRoomAdapter
import kc.ac.uc.clubplatform.adapters.SelectableChatAdapter
import kc.ac.uc.clubplatform.adapters.UserAdapter
import kc.ac.uc.clubplatform.models.ChatRoom
import kc.ac.uc.clubplatform.models.User
import kc.ac.uc.clubplatform.activity.ChatRoomActivity

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    // 채팅방 목록 (실제로는 DB에서 불러와야 함)
    private val chatRooms = mutableListOf<ChatRoom>()

    // 사용자 목록 (실제로는 DB에서 불러와야 함)
    private val users = mutableListOf<User>()

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

        // 샘플 사용자 데이터 추가
        users.add(User(1, "김철수", "20210001", R.drawable.default_profile))
        users.add(User(2, "이영희", "20210002", R.drawable.default_profile))
        users.add(User(3, "박민수", "20210003", R.drawable.default_profile))
        users.add(User(4, "정지원", "20220001", R.drawable.default_profile))
        users.add(User(5, "홍길동", "20220002", R.drawable.default_profile))

        setupRecyclerView()
        setupButtons()
    }

    private fun setupRecyclerView() {
        val adapter = ChatRoomAdapter(chatRooms) { chatRoom ->
            val intent = Intent(requireContext(), ChatRoomActivity::class.java)
            intent.putExtra("chatRoomName", chatRoom.name)
            startActivity(intent)
        }

        binding.rvChatRooms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatRooms.adapter = adapter
    }

    private fun setupButtons() {
        // 새 채팅 버튼
        binding.btnNewChat.setOnClickListener {
            showNewChatDialog()
        }

        // 채팅 삭제 버튼
        binding.btnDeleteChat.setOnClickListener {
            showDeleteChatDialog()
        }
    }
    
    private fun showNewChatDialog() {
        val dialog = Dialog(requireContext())
        val dialogBinding = DialogNewChatBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        
        // 다이얼로그 크기 조정
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        // 사용자 어댑터 설정
        val userAdapter = UserAdapter(users) { user ->
            // 사용자 선택 로직
            dialogBinding.btnStart.isEnabled = true
            Toast.makeText(requireContext(), "${user.name}님과 채팅을 시작합니다.", Toast.LENGTH_SHORT).show()
        }
        
        dialogBinding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.rvUsers.adapter = userAdapter
        
        // 초기 상태에서는 채팅 시작 버튼 비활성화
        dialogBinding.btnStart.isEnabled = false
        
        // 라디오 버튼 이벤트
        dialogBinding.rgChatType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbGroup) {
                dialogBinding.etSearch.hint = "그룹 멤버 검색"
            } else {
                dialogBinding.etSearch.hint = "이름 또는 학번으로 검색"
            }
        }
        
        // 검색 기능 구현
        dialogBinding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchQuery = s.toString().trim()
                if (searchQuery.isNotEmpty()) {
                    val filteredUsers = users.filter { 
                        it.name.contains(searchQuery, ignoreCase = true) || 
                        it.studentId.contains(searchQuery) 
                    }
                    userAdapter.updateUsers(filteredUsers)
                } else {
                    userAdapter.updateUsers(users)
                }
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // 버튼 이벤트
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.btnStart.setOnClickListener {
            // 선택된 사용자와 새 채팅 시작
            val selectedUser = userAdapter.getSelectedUser()
            if (selectedUser != null) {
                // 이미 존재하는 채팅방인지 확인
                val existingChatRoom = chatRooms.find { it.name == selectedUser.name }
                if (existingChatRoom != null) {
                    Toast.makeText(requireContext(), "이미 존재하는 채팅방입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    // 새 채팅방 추가
                    val newChatRoom = ChatRoom(
                        chatRooms.size + 1,
                        selectedUser.name,
                        "새 채팅이 시작되었습니다.",
                        "방금 전",
                        0
                    )
                    chatRooms.add(0, newChatRoom)  // 목록 맨 위에 추가
                    binding.rvChatRooms.adapter?.notifyItemInserted(0)
                    binding.rvChatRooms.scrollToPosition(0)
                    
                    Toast.makeText(requireContext(), "${selectedUser.name}님과 채팅을 시작합니다.", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showDeleteChatDialog() {
        if (chatRooms.isEmpty()) {
            Toast.makeText(requireContext(), "삭제할 채팅방이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dialog = Dialog(requireContext())
        val dialogBinding = DialogDeleteChatBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        
        // 다이얼로그 크기 조정
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        // 선택 가능한 채팅방 어댑터 설정
        val selectableChatAdapter = SelectableChatAdapter(chatRooms) { selectedCount ->
            dialogBinding.tvSelectedCount.text = "${selectedCount}개 선택됨"
            dialogBinding.btnDelete.isEnabled = selectedCount > 0
        }
        
        dialogBinding.rvChatsToDelete.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.rvChatsToDelete.adapter = selectableChatAdapter
        
        // 초기 상태에서는 삭제 버튼 비활성화
        dialogBinding.btnDelete.isEnabled = false
        
        // 버튼 이벤트
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.btnDelete.setOnClickListener {
            val selectedChats = selectableChatAdapter.getSelectedChats()
            if (selectedChats.isEmpty()) {
                Toast.makeText(requireContext(), "삭제할 채팅방을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 선택된 채팅방 삭제
            for (chatRoom in selectedChats) {
                val position = chatRooms.indexOf(chatRoom)
                chatRooms.remove(chatRoom)
                binding.rvChatRooms.adapter?.notifyItemRemoved(position)
            }
            
            Toast.makeText(requireContext(), "${selectedChats.size}개의 채팅방이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
