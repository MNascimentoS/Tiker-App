package br.com.tiker.core

import android.app.Application
import androidx.room.Room
import br.com.tiker.persistence.StickerExternalDatabase
import br.com.tiker.persistence.StickerRoomDatabase
import br.com.tiker.ui.allStickers.AllStickersFragment
import br.com.tiker.ui.allStickers.AllStickersViewModel
import br.com.tiker.ui.create.CreateViewModel
import br.com.tiker.ui.main.MainViewModel
import br.com.tiker.ui.myPackages.MyPackagesViewModel
import br.com.tiker.ui.stickerPackage.StickerPackageViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        StickerExternalDatabase.initialize()
        startKoin {
            modules(listOf(
                    module {
                        single {
                            Room
                                    .databaseBuilder(applicationContext, StickerRoomDatabase::class.java, StickerRoomDatabase.NAME)
                                    .build()
                        }
                        single { MainViewModel() }
                        factory { AllStickersViewModel() }
                        factory { CreateViewModel() }
                        factory { MyPackagesViewModel() }
                        factory { StickerPackageViewModel() }
                    }
            )).androidContext(applicationContext)
        }
    }
}