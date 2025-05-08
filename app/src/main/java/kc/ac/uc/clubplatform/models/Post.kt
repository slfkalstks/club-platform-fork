package kc.ac.uc.clubplatform.models

data class Post(
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val date: String,
    val viewCount: Int,
    val commentCount: Int
)