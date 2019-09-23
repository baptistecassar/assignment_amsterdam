package com.example.pinch.repository

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.example.pinch.data.service.GamesDataSource
import com.example.pinch.model.Game
import com.example.pinch.utils.NetworkState
import com.example.pinch.utils.PagingRequestHelper
import com.example.pinch.utils.createStatusLiveData
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor

/**
 * @author Baptiste Cassar
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 **/
class GamesBoundaryCallback(
    private val webservice: GamesDataSource,
    private val handleResponse: (List<Game>) -> Unit,
    private val ioExecutor: Executor,
    private val networkPageSize: Int
) : PagedList.BoundaryCallback<Game>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    var offsetCount = 0

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            webservice
                .getGames(size = networkPageSize)
                .handleWebservice(it)
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: Game) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            webservice
                .getGames(offsetCount, networkPageSize)
                .handleWebservice(it)
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Game) {
        // ignored, since we only ever append to what's in the DB
    }

    /**
     * When refresh is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */
    @MainThread
    fun refresh(handleRefresh: (List<Game>) -> Unit): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        webservice.getGames(size = networkPageSize)
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = { games ->
                    offsetCount = games.size
                    handleRefresh(games)
                    // since we are in bg thread now, post the result.
                    networkState.postValue(NetworkState.LOADED)
                },
                onError = {
                    // retrofit calls this on main thread so safe to call set value
                    networkState.value = NetworkState.error(it.message)
                }
            )
            .addTo(compositeDisposable)
        return networkState
    }

    /**
     * Clear all references
     **/
    fun cleared() {
        compositeDisposable.clear()
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
        games: List<Game>,
        it: PagingRequestHelper.Request.Callback
    ) {
        ioExecutor.execute {
            handleResponse(games)
            offsetCount += games.size
            it.recordSuccess()
        }
    }

    private fun Single<List<Game>>.handleWebservice(callback: PagingRequestHelper.Request.Callback) {
        this.subscribeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = { games ->
                    insertItemsIntoDb(games, callback)
                },
                onError = { throwable ->
                    callback.recordFailure(throwable)
                })
            .addTo(compositeDisposable)
    }
}