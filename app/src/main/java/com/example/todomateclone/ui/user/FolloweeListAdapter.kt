package com.example.todomateclone.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todomateclone.databinding.FollowListItemBinding
import com.example.todomateclone.databinding.TodoListItemBinding
import com.example.todomateclone.network.dto.FolloweeDTO
import com.example.todomateclone.network.dto.TaskDTO


class FolloweeListAdapter(private val onItemClicked1: (FolloweeDTO) -> Unit, private val onItemClicked2: (FolloweeDTO) -> Unit) : PagingDataAdapter<FolloweeDTO, FolloweeListAdapter.FolloweeViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolloweeViewHolder {
        return FolloweeViewHolder(
            FollowListItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: FolloweeViewHolder, position: Int) {
        val current = getItem(position)
        current?.let {
            holder.visitButton.setOnClickListener {
                onItemClicked1(current)
            }
            holder.unfollowButton.setOnClickListener {
                onItemClicked2(current)
                holder.visitButton.visibility= INVISIBLE
            }
            holder.bind(it)
        }
    }



    class FolloweeViewHolder(private var binding: FollowListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val visitButton = binding.visitButton
        val unfollowButton = binding.unfollowButton
        fun bind(followee: FolloweeDTO) {
            binding.apply {
                followupButton.visibility = INVISIBLE
                cancelfollowupButton.visibility = INVISIBLE
                visitButton.visibility = VISIBLE
                unfollowButton.visibility = VISIBLE
                deleteButton.visibility = INVISIBLE
                blockButton.visibility = INVISIBLE
                unblockButton.visibility = INVISIBLE
                emailTextView.setText(followee.to_user_email)
                nicknameTextView.setText("닉네임: "+followee.to_user_nickname)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<FolloweeDTO>() {
            override fun areItemsTheSame(oldItem: FolloweeDTO, newItem: FolloweeDTO): Boolean {
                return oldItem.to_user_id == newItem.to_user_id
            }

            override fun areContentsTheSame(oldItem: FolloweeDTO, newItem: FolloweeDTO): Boolean {
                return oldItem.to_user_email == newItem.to_user_email
            }
        }
    }
}
