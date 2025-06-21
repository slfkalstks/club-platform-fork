// adapters/NoticeAdapter.kt
package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.databinding.ItemNoticeBinding
import kc.ac.uc.clubplatform.models.PostInfo

class NoticeAdapter(
    private val notices: List<PostInfo>,
    private val onItemClick: (PostInfo) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val binding = ItemNoticeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoticeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(notices[position])
    }

    override fun getItemCount() = notices.size

    inner class NoticeViewHolder(private val binding: ItemNoticeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: PostInfo) {
            binding.tvTitle.text = post.title
            binding.tvAuthor.text = post.authorName
            binding.tvDate.text = post.createdAt
            binding.tvCommentCount.text = post.commentCount.toString()
            binding.tvViewCount.text = post.viewCount.toString()

            binding.root.setOnClickListener {
                onItemClick(post)
            }
        }
    }
}