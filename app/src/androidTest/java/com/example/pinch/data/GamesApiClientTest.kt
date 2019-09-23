package com.example.pinch.data

import com.example.pinch.data.service.GamesApiClient
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject

/**
 * @author Baptiste Cassar
 **/

class GamesApiClientTest : KoinTest {

    private val gamesApiClient: GamesApiClient by inject()

    @Test
    fun getGamesTest() {
        val test = gamesApiClient.getGames(size = 20).blockingGet()
        Assert.assertNotNull(test)
        Assert.assertEquals(20, test?.size)
        test?.forEach {
            if (it.coverId != null) {
                Assert.assertFalse(it.thumbnailUrl.isNullOrEmpty())
                Assert.assertFalse(it.coverUrl.isNullOrEmpty())
            }
        }
    }

    companion object {
        /**
         * Override default Koin configuration to use Room in-memory database
         */
        @BeforeClass
        @JvmStatic
        fun before() {
            loadKoinModules()
        }

        @AfterClass
        @JvmStatic
        fun after() {
            stopKoin()
        }
    }
}