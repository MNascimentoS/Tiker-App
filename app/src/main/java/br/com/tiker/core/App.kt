package br.com.tiker.core

import android.app.Application
import androidx.room.Room
import br.com.tiker.persistence.SaveBitmapToDevice
import br.com.tiker.persistence.StickerExternalDatabase
import br.com.tiker.persistence.StickerRoomDatabase
import br.com.tiker.persistence.StickerRoomDatabase.Companion.MIGRATION_1_2
import br.com.tiker.persistence.StickerRoomDatabase.Companion.MIGRATION_2_3
import br.com.tiker.ui.allStickers.AllStickersViewModel
import br.com.tiker.ui.create.CreateViewModel
import br.com.tiker.ui.main.MainViewModel
import br.com.tiker.ui.myPackages.MyPackagesViewModel
import br.com.tiker.ui.stickerPackage.StickerPackageViewModel
import com.facebook.soloader.SoLoader
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)
        StickerExternalDatabase.initialize()
        startKoin {
            modules(listOf(
                    module {
                        single {
                            Room
                                    .databaseBuilder(applicationContext, StickerRoomDatabase::class.java, StickerRoomDatabase.NAME)
                                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                                    .build()
                        }
                        single { SaveBitmapToDevice() }
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