package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.models.ChatRoom
import kc.ac.uc.clubplatform.databinding.ItemChatRoomBinding

// Continuing ChatRoomAdapter.kt
class ChatRoomAdapter(
    private val chatRooms: List<ChatRoom>,
    private val onItemClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val binding = ItemChatRoomBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatRoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(chatRooms[position])
    }

    override fun getItemCount() = chatRooms.size

    inner class ChatRoomViewHolder(private val binding: ItemChatRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatRoom: ChatRoom) {
            binding.tvChatRoomName.text = chatRoom.name
            binding.tvLastMessage.text = chatRoom.lastMessage
            binding.tvTime.text = chatRoom.time

            if (chatRoom.unreadCount > 0) {
                binding.tvUnreadCount.visibility = View.VISIBLE
                binding.tvUnreadCount.text = chatRoom.unreadCount.toString()
            } else {
                binding.tvUnreadCount.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onItemClick(chatRoom)
            }
        }
    }
}