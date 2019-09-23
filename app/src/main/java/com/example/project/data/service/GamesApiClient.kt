package com.example.project.data.service

import com.example.project.BuildConfig
import com.example.project.model.Cover
import com.example.project.model.Game
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author Baptiste Cassar
 **/
class GamesApiClient : GamesDataSource {

    private var client: GamesApiInterface

    /**
     * constructor for Api Client
     */
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        client = retrofit.create(GamesApiInterface::class.java)
    }

    /**
     * get OkHttpClient
     * sets connection timeout and read timeout with [TIMEOUT_SEC]
     * sets Interceptor with {@link #getInterceptor(Map, boolean, String)} and [httpLoggingInterceptor]
     * @return {@link OkHttpClient}
     */
    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SEC.toLong(), TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SEC.toLong(), TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT_SEC.toLong(), TimeUnit.SECONDS)
            .addInterceptor(getInterceptor())
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    /**
     * Interceptor use to inject the user-key into the request in process
     */
    private fun getInterceptor(): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            for ((key, value) in headers) {
                request = request.newBuilder().addHeader(key, value).build()
            }
            request = request.newBuilder()
                .addHeader(USER_KEY, BuildConfig.USER_KEY).build()
            chain.proceed(request)
        }
    }

    /**
     * calls ws to get the [Cover] linked to a [Game]
     * we use the [Cover.imageId] to get the cover and thumbnail images' urls of the game
     */
    private fun getGameImages(game: Game): Single<Game> =
        client.getGameImageId(game.coverId)
            .flatMap { list ->
                if (list.isNotEmpty() && !list[0].imageId.isNullOrBlank()) {
                    val cover = list[0]
                    game.coverUrl =
                        "https://images.igdb.com/igdb/image/upload/t_cover_big/" + cover.imageId + ".jpg"
                    game.thumbnailUrl =
                        "https://images.igdb.com/igdb/image/upload/t_thumb/" + cover.imageId + ".jpg"
                }
                Single.just(game)
            }

    /**
     * gets a list of games by calling ws
     * if [Game.coverId] is not null calls [getGameImages] to get the game's images' url
     */
    override fun getGames(offset: Int, size: Int) =
        client.getGames(GAMES_FIELDS, offset, size)
            .toObservable()
            .flatMapIterable { items -> items }
            .flatMap {
                if (it.coverId != null)
                    getGameImages(it).toObservable()
                else
                    Observable.just(it)
            }
            .toList()


    //================================================================================
    // companion object
    //================================================================================

    companion object {
        private val TAG = GamesApiClient::class.java.simpleName
        private const val TIMEOUT_SEC = 20
        private const val GAMES_FIELDS = "*"
        val headers: Map<String, String> = HashMap()
        private const val USER_KEY = "user-key"
        //http interceptor to be able to log the body of the http requests
        private val httpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(
                if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
            )
    }

}