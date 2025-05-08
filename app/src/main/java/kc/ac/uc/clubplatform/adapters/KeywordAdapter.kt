// adapters/KeywordAdapter.kt
package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.databinding.ItemKeywordBinding
import kc.ac.uc.clubplatform.models.Keyword

class KeywordAdapter(
    private val keywords: List<Keyword>,
    private val onDeleteClick: (Keyword) -> Unit
) : RecyclerView.Adapter<KeywordAdapter.KeywordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
        val binding = ItemKeywordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KeywordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        holder.bind(keywords[position])
    }

    override fun getItemCount() = keywords.size

    inner class KeywordViewHolder(private val binding: ItemKeywordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(keyword: Keyword) {
            binding.tvKeyword.text = keyword.keyword

            binding.btnDelete.setOnClickListener {
                onDeleteClick(keyword)
            }
        }
    }
}