package br.com.tiker.ui.allStickers

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import br.com.tiker.model.StickerModel
import br.com.tiker.persistence.StickerExternalDatabase
import br.com.tiker.persistence.StickerRoomDatabase
import br.com.tiker.persistence.model.StickerEntity
import br.com.tiker.persistence.model.StickerPackageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.ByteArrayOutputStream
import kotlin.coroutines.CoroutineContext

class AllStickersViewModel : ViewModel(), KoinComponent, CoroutineScope {

    private val stickerDb: StickerRoomDatabase by inject()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var _hasItems: MutableLiveData<Boolean> = MutableLiveData()
    var hasItems: LiveData<Boolean> = _hasItems

    private var _isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var isLoading: LiveData<Boolean> = _isLoading

    val stickerList: LiveData<PagedList<StickerModel>> =
        LivePagedListBuilder<Int, StickerModel>(StickerExternalDatabase.instance.stickerDataSourceFactory, 100)
            .setBoundaryCallback(object : PagedList.BoundaryCallback<StickerModel>() {
                override fun onZeroItemsLoaded() {
                    super.onZeroItemsLoaded()
                    _isLoading.value = false
                    _hasItems.value = false
                }

                override fun onItemAtEndLoaded(itemAtEnd: StickerModel) {
                    super.onItemAtEndLoaded(itemAtEnd)
                    _hasItems.value = true
                    _isLoading.value = false
                }

                override fun onItemAtFrontLoaded(itemAtFront: StickerModel) {
                    super.onItemAtFrontLoaded(itemAtFront)
                    _hasItems.value = true
                    _isLoading.value = false
                }
            })
            .build()

    fun saveTempStickerList(stickerList: ArrayList<StickerModel>) = launch {
        _isLoading.value = true
        val imageByteList = arrayListOf<StickerEntity>()
        stickerList.forEach {
            val stream = ByteArrayOutputStream()
            it.image.compress(Bitmap.CompressFormat.PNG, 100, stream)
            imageByteList.add(StickerEntity(sticker = stream.toByteArray()))
        }

        stickerDb.stickerDao().addStickerList(StickerPackageEntity(), imageByteList)
        _isLoading.value = false
    }

    fun forceRechargePackage() {

    }

}
