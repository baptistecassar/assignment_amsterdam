package com.example.pinch.repository

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import com.example.pinch.db.GameDatabase
import com.example.pinch.model.Game
import com.example.pinch.service.GamesApiClient
import com.example.pinch.utils.Listing
import com.example.pinch.utils.NetworkState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * @author Baptiste Cassar
 * @date 2019-09-20
 * Repository implementation that uses a database PagedList + a boundary callback to return a
 * listing that loads in pages.
 **/
class GamesRepository(
    private val db: GameDatabase,
    private val gamesApiClient: GamesApiClient,
    private val ioExecutor: Executor = Executors.newSingleThreadExecutor(),
    private val networkPageSize: Int = DEFAULT_NETWORK_PAGE_SIZE
) {
    companion object {
        private const val DEFAULT_NETWORK_PAGE_SIZE = 20
    }

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    private fun insertGamesIntoDb(gameList: List<Game>) {
        gameList.let { games ->
            db.runInTransaction {
                db.gameDao().insertGames(games)
            }
        }
    }


    /**
     * When refresh is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */
    @MainThread
    private fun refresh(boundaryCallback: GamesBoundaryCallback): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        gamesApiClient.getGames(size = networkPageSize)
            .enqueue(object : Callback<List<Game>> {
                /**
                 * Invoked when a network exception occurred talking to the server or when an unexpected
                 * exception occurred creating the request or processing the response.
                 */
                override fun onFailure(call: Call<List<Game>>, t: Throwable) {
                    // retrofit calls this on main thread so safe to call set value
                    networkState.value = NetworkState.error(t.message)
                }

                /**
                 * Invoked for a received HTTP response.
                 *
                 *
                 * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
                 * Call [Response.isSuccessful] to determine if the response indicates success.
                 */
                override fun onResponse(call: Call<List<Game>>, response: Response<List<Game>>) {
                    val list = response.body()
                    if (response.isSuccessful && list != null) {
                        ioExecutor.execute {
                            //we set the offset for boundary callback
                            boundaryCallback.offsetCount = list.size
                            db.runInTransaction {
                                db.clearAllTables()
                                insertGamesIntoDb(list)
                            }
                            // since we are in bg thread now, post the result.
                            networkState.postValue(NetworkState.LOADED)
                        }
                    } else {
                        networkState.postValue(NetworkState.error("error code: ${response.code()}"))
                    }
                }
            })
        return networkState
    }


    /**
     * Returns a Listing for the given subreddit.
     */
    @MainThread
    fun games(pageSize: Int = networkPageSize): Listing<Game> {
        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = GamesBoundaryCallback(
            webservice = gamesApiClient,
            handleResponse = this::insertGamesIntoDb,
            ioExecutor = ioExecutor,
            networkPageSize = networkPageSize
        )
        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refresh(boundaryCallback)
        }
        // We use toLiveData Kotlin extension function here, you could also use LivePagedListBuilder
        val livePagedList = db.gameDao().getGamesPaged().toLiveData(
            pageSize = pageSize,
            boundaryCallback = boundaryCallback
        )
        return Listing(
            pagedList = livePagedList,
            networkState = boundaryCallback.networkState,
            retry = {
                boundaryCallback.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }
}