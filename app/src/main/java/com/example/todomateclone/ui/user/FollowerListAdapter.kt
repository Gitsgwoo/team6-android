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
import com.example.todomateclone.network.dto.FollowerDTO
import com.example.todomateclone.network.dto.TaskDTO


class FollowerListAdapter(private val onItemClicked1: (FollowerDTO) -> Unit, private val onItemClicked2: (FollowerDTO) -> Unit, private val onItemClicked3: (FollowerDTO) -> Unit, private val onItemClicked4: (FollowerDTO) -> Unit) : PagingDataAdapter<FollowerDTO, FollowerListAdapter.FollowerViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        return FollowerViewHolder(
            FollowListItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        val current = getItem(position)

        current?.let {
            holder.deleteButton.setOnClickListener {
                holder.itemView.visibility = INVISIBLE
                onItemClicked1(current)
            }
            holder.blockButton.setOnClickListener {
                onItemClicked2(current)
            }
            holder.followupButton.setOnClickListener {
                onItemClicked3(current)
                holder.followupButton.visibility = INVISIBLE
                holder.cancelfollowupButton.visibility = VISIBLE
            }
            holder.cancelfollowupButton.setOnClickListener {
                onItemClicked4(current)
                holder.followupButton.visibility = VISIBLE
                holder.cancelfollowupButton.visibility = INVISIBLE
            }
            holder.bind(it)
        }
    }



    class FollowerViewHolder(private var binding: FollowListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val deleteButton = binding.deleteButton
        val blockButton = binding.blockButton
        val followupButton = binding.followupButton
        val cancelfollowupButton = binding.cancelfollowupButton
        fun bind(follower: FollowerDTO) {
            binding.apply {
                if(follower.is_following) {
                    followupButton.visibility = INVISIBLE
                    cancelfollowupButton.visibility = VISIBLE
                }
                else {
                    followupButton.visibility = VISIBLE
                    cancelfollowupButton.visibility = INVISIBLE
                }
                unfollowButton.visibility = INVISIBLE
                visitButton.visibility = INVISIBLE
                deleteButton.visibility = VISIBLE
                blockButton.visibility = VISIBLE
                unblockButton.visibility = INVISIBLE
                emailTextView.setText(follower.from_user_email)
                nicknameTextView.setText("닉네임: "+follower.from_user_nickname)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<FollowerDTO>() {
            override fun areItemsTheSame(oldItem: FollowerDTO, newItem: FollowerDTO): Boolean {
                return oldItem.from_user_id == newItem.from_user_id
            }

            override fun areContentsTheSame(oldItem: FollowerDTO, newItem: FollowerDTO): Boolean {
                return oldItem.from_user_email == newItem.from_user_email
            }
        }
    }
}
