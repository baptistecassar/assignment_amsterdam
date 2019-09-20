package com.example.pinch.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pinch.R
import com.example.pinch.databinding.ItemGameBinding
import com.example.pinch.databinding.ItemNetworkStateBinding
import com.example.pinch.model.Game
import com.example.pinch.utils.NetworkState

/**
 * @author Baptiste Cassar
 * @date 2019-09-18
 **/
class GameAdapter(private val retryCallback: () -> Unit) :
    PagedListAdapter<Game, RecyclerView.ViewHolder>(GAME_COMPARATOR) {

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_game -> GameViewHolder.create(parent)
            R.layout.item_network_state -> NetworkStateItemViewHolder.create(parent)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_game -> (holder as GameViewHolder).apply {
                binding.game = getItem(position)
            }
            R.layout.item_network_state -> (holder as NetworkStateItemViewHolder).apply {
                binding.networkState = networkState
                binding.retryListener = View.OnClickListener {
                    Log.v("TEST", "ETST")
                    retryCallback()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.item_network_state
        } else {
            R.layout.item_game
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    companion object {
        private val GAME_COMPARATOR = object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean =
                oldItem.key == newItem.key

            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean =
                oldItem == newItem
        }
    }


    class NetworkStateItemViewHolder(val binding: ItemNetworkStateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun create(parent: ViewGroup): NetworkStateItemViewHolder {
                val binding = ItemNetworkStateBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
                return NetworkStateItemViewHolder(binding)
            }
        }
    }

    class GameViewHolder(val binding: ItemGameBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun create(parent: ViewGroup): GameViewHolder {
                val binding = ItemGameBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
                return GameViewHolder(binding)
            }
        }
    }
}