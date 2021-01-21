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
import android.graphics.ImageDecoder
import android.os.Build
import androidx.lifecycle.LiveData
import br.com.tiker.model.StickerModel
import br.com.tiker.persistence.StickerRoomDatabase
import br.com.tiker.utils.random
import java.io.File
import kotlin.random.Random


class CreateViewModel : ViewModel(), KoinComponent, CoroutineScope {

    private val stickerDb: StickerRoomDatabase by inject()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    var stickerPackageId = 0

    var isLoading: MutableLiveData<Boolean> = MutableLiveData()

    private var _stickerList: MutableLiveData<List<StickerModel>> = MutableLiveData()
    var stickerList: LiveData<List<StickerModel>> = _stickerList

    var callShareActivity: MutableLiveData<Boolean> = MutableLiveData()

    fun loadUnsavedStickerList() = launch {
        val result = stickerDb.stickerDao().getLastUnsavedStickerListAllData()
        if (result.first != null && result.second != null) {
            stickerPackageId = result.first!!.id
            val list = arrayListOf<StickerModel>()
            result.second!!.forEachIndexed { index, value ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && value.stickerFile != null) {
                    list.add(
                        StickerModel(
                            index,
                            BitmapFactory.decodeFile(value.stickerFile),
                            ImageDecoder.decodeDrawable(ImageDecoder.createSource(File(value.stickerFile!!))),
                            filePath = value.stickerFile ?: ""
                        )
                    )
                } else {
                    list.add(
                        StickerModel(
                            index,
                            BitmapFactory.decodeByteArray(value.sticker, 0, value.sticker.size),
                            filePath = value.stickerFile ?: ""
                        )
                    )
                }
            }
            _stickerList.value = list
        }
        isLoading.value = false
    }

    fun saveStickerPackage(name: String, author: String) = launch {
        val identifier = random() + Random.nextInt()
        stickerDb.stickerDao()
            .saveLastUnsavedStickerList(stickerPackageId, identifier, name, author)
        callShareActivity.value = true
    }

}
