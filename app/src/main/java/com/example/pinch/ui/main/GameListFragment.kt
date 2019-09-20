package com.example.pinch.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pinch.R
import com.example.pinch.model.Game
import com.example.pinch.utils.NetworkState
import kotlinx.android.synthetic.main.main_fragment.*
import org.koin.android.viewmodel.ext.android.viewModel

class GameListFragment : Fragment() {

    private val gameListViewModelPaging by viewModel<GameListViewModel>()

    private var adapter = GameAdapter {
        gameListViewModelPaging.retry()
    }

    companion object {
        fun newInstance() = GameListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initSwipeToRefresh()
    }

    private fun initAdapter() {
        list_game.layoutManager = LinearLayoutManager(activity)
        list_game.adapter = adapter
        /**
         * Observe changes in the list of games
         */
        gameListViewModelPaging.dataSource.observe(this, Observer<PagedList<Game>> {
            adapter.submitList(it)
        })
        gameListViewModelPaging.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun initSwipeToRefresh() {
        gameListViewModelPaging.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            gameListViewModelPaging.refresh()
        }
    }

}
