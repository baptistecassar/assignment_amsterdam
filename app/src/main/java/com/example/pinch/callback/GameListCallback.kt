package com.example.pinch.callback

import com.example.pinch.model.Game
import com.example.pinch.ui.main.GameAdapter
import com.example.pinch.ui.main.GameListFragment

/**
 * @author Baptiste Cassar
 * this interface is used to handle click action on the items of the game list
 * bounded in [GameAdapter]
 * implemented in [GameListFragment]
 **/
interface GameListCallback {
    fun openGame(game: Game)
}