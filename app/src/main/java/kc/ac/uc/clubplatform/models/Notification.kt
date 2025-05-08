package kc.ac.uc.clubplatform.models

data class Notification(
    val id: Int,
    val type: String,
    val message: String,
    val date: String,
    val isRead: Boolean
)