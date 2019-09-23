package com.example.pinch.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.pinch.model.Game
import com.example.pinch.repository.GamesRepository
import com.example.pinch.utils.Listing

/**
 * @author Baptiste Cassar
 */

class GameListViewModel(
    private val gamesRepository: GamesRepository
) : ViewModel() {

    private val listing: LiveData<Listing<Game>> by lazy {
        liveData(gamesRepository.games())
    }

    //private val boundaryCallback = Transformations.switchMap(listing) { it.getBoundaryCallback() }
    val dataSource = Transformations.switchMap(listing) { it.pagedList }
    val networkState = Transformations.switchMap(listing) { it.networkState }
    val refreshState = Transformations.switchMap(listing) { it.refreshState }

    fun retry() {
        val listing = listing.value
        listing?.retry?.invoke()
    }

    fun refresh() {
        listing.value?.refresh?.invoke()
    }

    /**
     * Cleared all references and petitions boundary callback
     */
    override fun onCleared() {
        listing.value?.clear?.invoke()
    }
}

fun <T> liveData(data: T): LiveData<T> {
    val mld = MutableLiveData<T>()
    mld.value = data
    return mld
}