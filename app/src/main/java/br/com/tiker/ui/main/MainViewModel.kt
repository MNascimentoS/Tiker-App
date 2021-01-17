package br.com.tiker.ui.main

import androidx.lifecycle.ViewModel
import br.com.tiker.persistence.StickerRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext


class MainViewModel : ViewModel(), KoinComponent, CoroutineScope {

    private val stickerDb: StickerRoomDatabase by inject()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun removeAllUnsavedStickers() = launch {
        stickerDb.stickerDao().removeAllUnsavedStickersPackageList()
    }

}
