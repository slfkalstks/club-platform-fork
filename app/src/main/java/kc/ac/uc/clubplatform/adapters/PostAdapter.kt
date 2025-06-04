// adapters/PostAdapter.kt
package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.R
import kc.ac.uc.clubplatform.databinding.ItemPostBinding
import kc.ac.uc.clubplatform.models.Post

class PostAdapter(
    private val posts: List<Post>,
    private val onItemClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.tvTitle.text = post.title
            binding.tvContent.text = post.content
            binding.tvAuthor.text = post.author
            binding.tvDate.text = post.date
            binding.tvCommentCount.text = post.commentCount.toString()
            binding.tvViewCount.text = post.viewCount.toString()

            // 게시글 출처 태그 표시
            if (post.isNotice) {
                binding.tvPostTag.text = "공지"
                binding.tvPostTag.setBackgroundResource(R.drawable.badge_background)
                binding.tvPostTag.visibility = View.VISIBLE
            } else {
                binding.tvPostTag.text = "일반"
                binding.tvPostTag.setBackgroundResource(R.drawable.badge_light_background)
                binding.tvPostTag.visibility = View.VISIBLE
            }

            binding.root.setOnClickListener {
                onItemClick(post)
            }
        }
    }
}