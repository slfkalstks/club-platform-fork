package kc.ac.uc.clubplatform.models

data class ChatRoom(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int
)
