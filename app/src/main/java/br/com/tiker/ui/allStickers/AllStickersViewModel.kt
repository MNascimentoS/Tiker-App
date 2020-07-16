package br.com.tiker.ui.allStickers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.arch.core.util.Function
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import br.com.tiker.model.StickerModel
import br.com.tiker.persistence.StickerExternalDatabase
import br.com.tiker.persistence.StickerRoomDatabase
import br.com.tiker.persistence.dataSource.StickerDataSource
import br.com.tiker.persistence.dataSource.StickerDataSourceFactory
import br.com.tiker.persistence.model.StickerEntity
import br.com.tiker.persistence.model.StickerPackageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors


class AllStickersViewModel : ViewModel(), KoinComponent {

    private val stickerDb: StickerRoomDatabase by inject()

    private var _hasItems: MutableLiveData<Boolean> = MutableLiveData()
    var hasItems: LiveData<Boolean> = _hasItems

    private var stickerDataSourceFactory: StickerDataSourceFactory
    var stickerList: LiveData<PagedList<StickerModel>>


    private var _isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var isLoading: LiveData<Boolean> = _isLoading

    init {
        val executor = Executors.newFixedThreadPool(10)
        stickerDataSourceFactory = StickerExternalDatabase.instance.stickerDataSourceFactory

        stickerDataSourceFactory.getMutableLiveData()?.observeForever { dataSource ->
            dataSource.loading.observeForever {
                _isLoading.postValue(it)
            }
        }

        val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(200)
                .setPageSize(200).build()

        stickerList = LivePagedListBuilder<Int, StickerModel>(stickerDataSourceFactory, pagedListConfig)
                .setFetchExecutor(executor)
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
    }

    fun saveTempStickerList(stickerList: ArrayList<StickerModel>) = viewModelScope.launch {
        _isLoading.value = true
        val imageByteList = arrayListOf<StickerEntity>()
        stickerList.forEach {
            val stream = ByteArrayOutputStream()
            val file = File(it.filePath)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            imageByteList.add(StickerEntity(sticker = stream.toByteArray()))
        }

        stickerDb.stickerDao().addStickerList(StickerPackageEntity(), imageByteList)
        _isLoading.value = false
    }

}
