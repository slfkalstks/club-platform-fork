package kc.ac.uc.clubplatform

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kc.ac.uc.clubplatform.adapters.MessageAdapter
import kc.ac.uc.clubplatform.databinding.ActivityChatRoomBinding
import kc.ac.uc.clubplatform.models.Message

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 채팅방 이름 설정
        val chatRoomName = intent.getStringExtra("chatRoomName") ?: "채팅방"
        binding.tvChatRoomName.text = chatRoomName

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter
    }

    private fun setupListeners() {
        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 메시지 전송 버튼
        binding.ivSend.setOnClickListener {
            val messageText = binding.etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message = Message("나", messageText, "방금")
                messages.add(message)
                adapter.notifyItemInserted(messages.size - 1)
                binding.rvMessages.scrollToPosition(messages.size - 1)
                binding.etMessage.text.clear()
            }
        }
    }
}