package kc.ac.uc.clubplatform.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val date: String,
    val viewCount: Int,
    val commentCount: Int,
    val isNotice: Boolean = false
) : Parcelable
