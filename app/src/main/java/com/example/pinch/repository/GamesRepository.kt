package com.example.pinch.repository

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import com.example.pinch.data.db.GameDatabase
import com.example.pinch.data.service.GamesDataSource
import com.example.pinch.model.Game
import com.example.pinch.utils.Listing
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * @author Baptiste Cassar
 * Repository implementation that uses a database PagedList + a boundary callback to return a
 * listing that loads in pages.
 **/
class GamesRepository(
    private val db: GameDatabase,
    private val webservice: GamesDataSource,
    private val ioExecutor: Executor = Executors.newSingleThreadExecutor(),
    private val networkPageSize: Int = DEFAULT_NETWORK_PAGE_SIZE
) {
    companion object {
        private const val DEFAULT_NETWORK_PAGE_SIZE = 5
    }

    /**
     * when data is refreshed cleans the database
     * then calls [insertGamesIntoDb] to populate with new data
     */
    private fun refreshDb(gameList: List<Game>) {
        db.runInTransaction {
            db.clearAllTables()
            insertGamesIntoDb(gameList)
        }
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
     * Returns a Listing for the given subreddit.
     */
    @MainThread
    fun games(pageSize: Int = networkPageSize): Listing<Game> {
        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = GamesBoundaryCallback(
            webservice = webservice,
            handleResponse = this::insertGamesIntoDb,
            ioExecutor = ioExecutor,
            networkPageSize = pageSize
        )
        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            //refresh(boundaryCallback)
            boundaryCallback.refresh(this::refreshDb)
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
            clear = {
                boundaryCallback.cleared()
            },
            refreshState = refreshState
        )
    }
}