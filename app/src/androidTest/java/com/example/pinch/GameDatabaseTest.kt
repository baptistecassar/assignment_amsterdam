package com.example.pinch

import androidx.annotation.Nullable
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.pinch.data.db.GameDatabase
import com.example.pinch.data.db.GamesDao
import com.example.pinch.model.Game
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * @author Baptiste Cassar
 * @date 2019-09-18
 **/
class GameDatabaseTest : KoinTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val gamesDao: GamesDao by inject()
    private val gameDatabase: GameDatabase by inject()

    @Test
    fun clear() {
        gameDatabase.clearAllTables()
        val test = gamesDao.getGames().getValueTesting()
        assertEquals(listOf<Game>(), test)
    }

    @Test
    fun addGame() {
        clear()
        val game = Game(1, 1, name = "game1", summary = "summary1")
        gamesDao.insertGame(game)
        val test = gamesDao.getGames().getValueTesting()
        assertEquals(1, test?.size)
        assertEquals(game.gameId, test?.get(0)?.gameId)
    }

    @Test
    fun editGame() {
        addGame()
        val game = Game(1, 1, name = "game edited", summary = "summary1")
        gamesDao.insertGames(listOf(game))
        val test = gamesDao.getGames().getValueTesting()
        assertEquals(1, test?.size)
        assertEquals(game.name, test?.get(0)?.name)
    }

    @Test
    fun addGames() {
        addGame()
        val game = Game(2, 2, name = "game2", summary = "summary2")
        gamesDao.insertGames(listOf(game))
        val test = gamesDao.getGames().getValueTesting()
        assertEquals(2, test?.size)
    }
}

@Throws(InterruptedException::class)
fun <T> LiveData<T>.getValueTesting(): T? {
    val data = arrayOfNulls<Any>(1)
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(@Nullable o: T) {
            data[0] = o
            latch.countDown()
            removeObserver(this)
        }
    }
    observeForever(observer)
    latch.await(2, TimeUnit.SECONDS)
    return data[0] as T?
}