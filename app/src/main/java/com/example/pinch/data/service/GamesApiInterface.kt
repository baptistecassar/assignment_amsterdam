package com.example.pinch.data.service

import com.example.pinch.model.Cover
import com.example.pinch.model.Game
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * @author Baptiste Cassar
 **/
interface GamesApiInterface {

    @GET("games")
    fun getGames(
        @Query("fields") fields: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Single<List<Game>>

    @GET("covers/{id}")
    fun getGameImageId(
        @Path(value = "id", encoded = true) id: Int?,
        @Query("fields") fields: String = "image_id"
    ): Single<List<Cover>>

}