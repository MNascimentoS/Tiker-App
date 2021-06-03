package br.com.tiker.ui.myPackages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import br.com.tiker.model.StickerPackageModel
import br.com.tiker.persistence.StickerExternalDatabase
import br.com.tiker.persistence.StickerRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

class MyPackagesViewModel : ViewModel(), KoinComponent, CoroutineScope {

    private val stickerDb: StickerRoomDatabase by inject()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var _hasItems: MutableLiveData<Boolean> = MutableLiveData()
    var hasItems: LiveData<Boolean> = _hasItems

    private var _isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var isLoading: LiveData<Boolean> = _isLoading

    val stickerPackageList: LiveData<PagedList<StickerPackageModel>> =
        LivePagedListBuilder(StickerExternalDatabase.instance.packageStickerDataSourceFactory, 10)
            .setBoundaryCallback(object : PagedList.BoundaryCallback<StickerPackageModel>() {
                override fun onZeroItemsLoaded() {
                    super.onZeroItemsLoaded()
                    _isLoading.value = false
                    _hasItems.value = false
                }

                override fun onItemAtEndLoaded(itemAtEnd: StickerPackageModel) {
                    super.onItemAtEndLoaded(itemAtEnd)
                    _isLoading.value = false
                    _hasItems.value = true
                }

                override fun onItemAtFrontLoaded(itemAtFront: StickerPackageModel) {
                    super.onItemAtFrontLoaded(itemAtFront)
                    _isLoading.value = false
                    _hasItems.value = true
                }
            })
            .build()

    fun forceRechargePackage() {
        stickerPackageList.value?.dataSource?.invalidate()
    }

    fun removeStickerPackage(id: Int) {
        launch {
            stickerDb.stickerDao().removeSavedStickersPackageList(id)
            forceRechargePackage()
        }
    }

}
