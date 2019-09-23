package com.example.pinch.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pinch.model.Game

/**
 * @author Baptiste Cassar
 * The Room Database that contains the Game table.
 **/

@Database(
    entities = [Game::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GamesDao
}