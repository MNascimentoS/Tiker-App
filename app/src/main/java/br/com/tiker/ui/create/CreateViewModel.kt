package br.com.tiker.ui.create

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext
import android.graphics.BitmapFactory
import br.com.tiker.persistence.StickerRoomDatabase
import br.com.tiker.utils.random
import kotlin.random.Random


class CreateViewModel : ViewModel(), KoinComponent, CoroutineScope {

    private val stickerDb: StickerRoomDatabase by inject()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    var stickerPackageId = 0

    var isLoading: MutableLiveData<Boolean> = MutableLiveData()

    var stickerList: MutableLiveData<List<Bitmap>> = MutableLiveData()

    var callShareActivity: MutableLiveData<Boolean> = MutableLiveData()

    fun loadUnsavedStickerList() = launch {
        val result = stickerDb.stickerDao().getLastUnsavedStickerListAllData()
        if (result.first != null && result.second != null) {
            stickerPackageId = result.first!!.id
            val bitmapList = arrayListOf<Bitmap>()
            result.second!!.forEach {
                val bmp = BitmapFactory.decodeByteArray(it.sticker, 0, it.sticker.size)
                bitmapList.add(bmp)
            }
            stickerList.value = bitmapList
        }
        isLoading.value = false
    }

    fun saveStickerPackage(name: String, author: String) = launch {
        val identifier = random() + Random.nextInt()
        stickerDb.stickerDao().saveLastUnsavedStickerList(stickerPackageId, identifier, name, author)
        callShareActivity.value = true
    }

}
