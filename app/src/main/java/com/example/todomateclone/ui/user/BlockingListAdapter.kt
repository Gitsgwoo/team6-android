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
import com.example.todomateclone.network.dto.BlockingDTO
import com.example.todomateclone.network.dto.FolloweeDTO
import com.example.todomateclone.network.dto.TaskDTO


class BlockingListAdapter(private val onItemClicked1: (BlockingDTO) -> Unit) : PagingDataAdapter<BlockingDTO, BlockingListAdapter.BlockingViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockingViewHolder {
        return BlockingViewHolder(
            FollowListItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: BlockingViewHolder, position: Int) {
        val current = getItem(position)
        current?.let {
            holder.unblockButton.setOnClickListener {
                onItemClicked1(current)
                holder.itemView.visibility = INVISIBLE
            }
            holder.bind(it)
        }
    }



    class BlockingViewHolder(private var binding: FollowListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val unblockButton = binding.unblockButton
        fun bind(blocking: BlockingDTO) {
            binding.apply {
                followupButton.visibility = INVISIBLE
                cancelfollowupButton.visibility = INVISIBLE
                visitButton.visibility = INVISIBLE
                unfollowButton.visibility = INVISIBLE
                deleteButton.visibility = INVISIBLE
                blockButton.visibility = INVISIBLE
                unblockButton.visibility = VISIBLE
                emailTextView.setText(blocking.to_user_email)
                nicknameTextView.setText("닉네임: "+blocking.to_user_nickname)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<BlockingDTO>() {
            override fun areItemsTheSame(oldItem: BlockingDTO, newItem: BlockingDTO): Boolean {
                return oldItem.to_user_id == newItem.to_user_id
            }

            override fun areContentsTheSame(oldItem: BlockingDTO, newItem: BlockingDTO): Boolean {
                return oldItem.to_user_email == newItem.to_user_email
            }
        }
    }
}
