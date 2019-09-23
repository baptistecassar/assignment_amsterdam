package com.example.project.callback

import com.example.project.model.Game
import com.example.project.ui.main.GameAdapter
import com.example.project.ui.main.GameListFragment

/**
 * @author Baptiste Cassar
 * this interface is used to handle click action on the items of the game list
 * bounded in [GameAdapter]
 * implemented in [GameListFragment]
 **/
interface GameListCallback {
    fun openGame(game: Game)
}