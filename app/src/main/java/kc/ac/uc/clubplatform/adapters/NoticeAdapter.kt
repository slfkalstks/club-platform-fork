// adapters/NoticeAdapter.kt
package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.databinding.ItemNoticeBinding
import kc.ac.uc.clubplatform.models.Post

class NoticeAdapter(
    private val notices: List<Post>,
    private val onItemClick: (Post) -> Unit
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

        fun bind(post: Post) {
            binding.tvTitle.text = post.title
            binding.tvAuthor.text = post.author
            binding.tvDate.text = post.date
            binding.tvCommentCount.text = post.commentCount.toString()
            binding.tvViewCount.text = post.viewCount.toString()

            binding.root.setOnClickListener {
                onItemClick(post)
            }
        }
    }
}