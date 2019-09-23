package com.example.pinch.data.service

import com.example.pinch.model.Game
import io.reactivex.Single

/**
 * @author Baptiste Cassar
 * remote entry point for accessing games data.
 **/
interface GamesDataSource {
    fun getGames(offset: Int = 0, size: Int): Single<List<Game>>
}