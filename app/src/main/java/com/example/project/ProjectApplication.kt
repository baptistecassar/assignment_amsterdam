package com.example.project

import android.app.Application
import androidx.room.Room
import com.example.project.data.db.GameDatabase
import com.example.project.data.service.GamesApiClient
import com.example.project.repository.GamesRepository
import com.example.project.ui.main.GameListViewModel
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
class ProjectApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Start Koin
        startKoin {
            androidLogger()
            androidContext(this@ProjectApplication)
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
            single {
                val apiClient: GamesApiClient = get()
                GamesRepository(get(), apiClient)
            }
        }
        // declared ViewModel using the viewModel keyword
        val viewModelModule = module {
            viewModel { GameListViewModel(get()) }
        }
    }

}