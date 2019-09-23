package com.example.project.ui.main

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
import com.example.project.callback.GameListCallback
import com.example.project.databinding.FragmentGameListBinding
import com.example.project.model.Game
import com.example.project.utils.NetworkState
import kotlinx.android.synthetic.main.fragment_game_list.*
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * @author Baptiste Cassar
 * displays a list of games paginated with [GameAdapter]
 * handles click on item of the list by opening [GameDetailsFragment]
 */

class GameListFragment : Fragment(), GameListCallback {

    private lateinit var binding: FragmentGameListBinding

    private val gameListViewModelPaging by viewModel<GameListViewModel>()

    private var adapter = GameAdapter(this) {
        gameListViewModelPaging.retry()
    }

    companion object {
        fun newInstance() = GameListFragment()
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
        //Observes changes in the list of games
        gameListViewModelPaging.dataSource.observe(this, Observer<PagedList<Game>> {
            adapter.submitList(it)
        })
        //Observes changes in the network state
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

    /**
     * callback called to handle click on an item of the list
     * opens [GameDetailsFragment]
     */
    override fun openGame(game: Game) {
        (activity as? MainActivity)?.startFragment(GameDetailsFragment.newInstance(game))
    }

}
