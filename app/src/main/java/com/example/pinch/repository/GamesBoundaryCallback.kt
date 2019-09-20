package com.example.pinch.repository

import androidx.annotation.MainThread
import androidx.paging.PagedList
import com.example.pinch.model.Game
import com.example.pinch.service.GamesApiClient
import com.example.pinch.utils.PagingRequestHelper
import com.example.pinch.utils.createStatusLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * @author Baptiste Cassar
 * @date 2019-09-20
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 **/
class GamesBoundaryCallback(
    private val webservice: GamesApiClient,
    private val handleResponse: (List<Game>) -> Unit,
    private val ioExecutor: Executor,
    private val networkPageSize: Int
) : PagedList.BoundaryCallback<Game>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()
    var offsetCount = 0

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            webservice
                .getGames(size = networkPageSize)
                .enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: Game) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            webservice.getGames(
                offsetCount,
                networkPageSize
            ).enqueue(createWebserviceCallback(it))
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Game) {
        // ignored, since we only ever append to what's in the DB
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

    private fun createWebserviceCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<List<Game>> {
        return object : Callback<List<Game>> {
            override fun onFailure(call: Call<List<Game>>, t: Throwable) {
                it.recordFailure(t)
            }

            override fun onResponse(call: Call<List<Game>>, response: Response<List<Game>>) {
                val list = response.body()
                if (response.isSuccessful && list != null) {
                    insertItemsIntoDb(list, it)
                } else {
                    it.recordFailure(Throwable("error code: ${response.code()}"))
                }
            }
        }
    }
}