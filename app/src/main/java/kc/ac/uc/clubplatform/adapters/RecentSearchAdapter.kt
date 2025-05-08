// adapters/RecentSearchAdapter.kt
package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.databinding.ItemRecentSearchBinding
import kc.ac.uc.clubplatform.models.RecentSearch

class RecentSearchAdapter(
    private val recentSearches: List<RecentSearch>,
    private val onItemClick: (RecentSearch) -> Unit,
    private val onDeleteClick: (RecentSearch) -> Unit
) : RecyclerView.Adapter<RecentSearchAdapter.RecentSearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchViewHolder {
        val binding = ItemRecentSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecentSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentSearchViewHolder, position: Int) {
        holder.bind(recentSearches[position])
    }

    override fun getItemCount() = recentSearches.size

    inner class RecentSearchViewHolder(private val binding: ItemRecentSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recentSearch: RecentSearch) {
            binding.tvQuery.text = recentSearch.query

            binding.root.setOnClickListener {
                onItemClick(recentSearch)
            }

            binding.ivDelete.setOnClickListener {
                onDeleteClick(recentSearch)
            }
        }
    }
}