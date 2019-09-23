package com.example.pinch

import com.example.pinch.data.service.GamesApiClient
import org.junit.Assert
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.inject

/**
 * @author Baptiste Cassar
 * @date 2019-09-18
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
}