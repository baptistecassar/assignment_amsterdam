package com.example.pinch

import android.app.Application
import androidx.room.Room
import com.example.pinch.data.db.GameDatabase
import com.example.pinch.data.service.GamesApiClient
import com.example.pinch.repository.GamesRepository
import com.example.pinch.ui.main.GameListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * @author Baptiste Cassar
 * uses Koin as Dependency Injection framework
 * declares the modules used in this project
 **/
class PinchApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Start Koin
        startKoin {
            androidLogger()
            androidContext(this@PinchApplication)
            modules(appDataModule + appDbModule + viewModelModule)
        }
    }

    //================================================================================
    // companion object
    //================================================================================

    companion object {
        val appDataModule = module {
            single { GamesApiClient() }
        }
        val appDbModule = module {
            single {
                Room.databaseBuilder(get(), GameDatabase::class.java, "games_database").build()
            }
            single { get<GameDatabase>().gameDao() }
            single { GamesRepository(get(), get()) }
        }
        // declared ViewModel using the viewModel keyword
        val viewModelModule = module {
            viewModel { GameListViewModel(get()) }
        }
    }

}