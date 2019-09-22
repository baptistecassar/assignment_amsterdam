package com.example.pinch.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pinch.MainActivity
import com.example.pinch.callback.GameListCallback
import com.example.pinch.databinding.FragmentGameListBinding
import com.example.pinch.model.Game
import com.example.pinch.utils.NetworkState
import kotlinx.android.synthetic.main.fragment_game_list.*
import org.koin.android.viewmodel.ext.android.viewModel


class GameListFragment : Fragment(), GameListCallback {

    private lateinit var binding: FragmentGameListBinding

    private val gameListViewModelPaging by viewModel<GameListViewModel>()

    private var adapter = GameAdapter(this) {
        gameListViewModelPaging.retry()
    }

    companion object {
        fun newInstance() = GameListFragment()
    }

    override fun onResume() {
        super.onResume()
        //(activity as? MainActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameListBinding.inflate(inflater)
        (activity as? MainActivity)?.setSupportActionBar(binding.toolbar as Toolbar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initSwipeToRefresh()
    }

    private fun initAdapter() {
        list_game.layoutManager = LinearLayoutManager(activity)
        list_game.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
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

    //================================================================================
    // @implements GameListCallback
    //================================================================================

    override fun openGame(game: Game) {
        (activity as? MainActivity)?.startFragment(GameDetailsFragment.newInstance(game))
    }

}
