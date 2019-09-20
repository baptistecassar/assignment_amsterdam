package com.example.pinch.db

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pinch.model.Game

/**
 * @author Baptiste Cassar
 * @date 2019-09-18
 * Data Access Object for the games table.
 **/
@Dao
interface GamesDao {

    /**
     * Select all games from the games table.
     *
     * @return all games.
     */
    @Query("SELECT * FROM games")
    fun getGames(): LiveData<List<Game>>

    /**
     * Select games from the games table.
     *
     * @return games.
     */
    @Query("SELECT * FROM games")
    fun getGamesPaged(): DataSource.Factory<Int, Game>

    /**
     * Insert a list of games in the database. If the games already exist, replace it.
     *
     * @param gameList the list of games to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGames(gameList: List<Game>)

    /**
     * Insert a game in the database. If the game already exists, replace it.
     *
     * @param game the game to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGame(game: Game)
}