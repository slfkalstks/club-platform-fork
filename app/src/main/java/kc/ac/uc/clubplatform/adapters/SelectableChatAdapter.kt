package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.databinding.ItemChatSelectableBinding
import kc.ac.uc.clubplatform.models.ChatRoom

class SelectableChatAdapter(
    private val chatRooms: List<ChatRoom>,
    private val onSelectionChangedListener: (Int) -> Unit
) : RecyclerView.Adapter<SelectableChatAdapter.SelectableChatViewHolder>() {

    private val selectedChats = mutableSetOf<ChatRoom>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableChatViewHolder {
        val binding = ItemChatSelectableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SelectableChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectableChatViewHolder, position: Int) {
        val chatRoom = chatRooms[position]
        holder.bind(chatRoom)
    }

    override fun getItemCount(): Int = chatRooms.size

    fun getSelectedChats(): List<ChatRoom> = selectedChats.toList()

    inner class SelectableChatViewHolder(private val binding: ItemChatSelectableBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatRoom: ChatRoom) {
            binding.tvChatName.text = chatRoom.name
            binding.tvLastMessage.text = chatRoom.lastMessage
            binding.tvTimestamp.text = chatRoom.time
            
            // 체크박스 상태 설정
            binding.checkboxSelect.isChecked = selectedChats.contains(chatRoom)

            // 클릭 리스너 설정
            binding.root.setOnClickListener {
                binding.checkboxSelect.isChecked = !binding.checkboxSelect.isChecked
                toggleSelection(chatRoom)
            }

            binding.checkboxSelect.setOnClickListener {
                toggleSelection(chatRoom)
            }
        }

        private fun toggleSelection(chatRoom: ChatRoom) {
            if (binding.checkboxSelect.isChecked) {
                selectedChats.add(chatRoom)
            } else {
                selectedChats.remove(chatRoom)
            }
            onSelectionChangedListener(selectedChats.size)
        }
    }
}
