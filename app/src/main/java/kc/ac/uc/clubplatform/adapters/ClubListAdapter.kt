package kc.ac.uc.clubplatform.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kc.ac.uc.clubplatform.databinding.ItemClubBinding
import kc.ac.uc.clubplatform.models.Club

class ClubListAdapter(
    private val clubs: List<Club>,
    private val onItemClick: (Club) -> Unit
) : RecyclerView.Adapter<ClubListAdapter.ClubViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubViewHolder {
        val binding = ItemClubBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClubViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClubViewHolder, position: Int) {
        holder.bind(clubs[position])
    }

    override fun getItemCount() = clubs.size

    inner class ClubViewHolder(private val binding: ItemClubBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(club: Club) {
            binding.tvClubName.text = club.name
            binding.tvClubDescription.text = club.description ?: "설명이 없습니다"
            binding.tvClubCategory.text = club.category?.name ?: "카테고리 없음"
            binding.tvMemberCount.text = "${club.memberCount ?: 0}명"

            // 내 역할 표시
            when (club.myRole) {
                "owner" -> binding.tvMyRole.text = "운영자"
                "admin" -> binding.tvMyRole.text = "관리자"
                "member" -> binding.tvMyRole.text = "회원"
                else -> binding.tvMyRole.text = ""
            }

            binding.root.setOnClickListener {
                onItemClick(club)
            }
        }
    }
}