package com.example.pinch.callback

import com.example.pinch.model.Game

/**
 * @author Baptiste Cassar
 * @date 2019-09-21
 **/
interface GameListCallback {
    fun openGame(game: Game)
}