package br.com.tiker.ui.myPackages

import androidx.lifecycle.LiveData
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

    val stickerPackageList: LiveData<PagedList<StickerPackageModel>> =
        LivePagedListBuilder<Int, StickerPackageModel>(StickerExternalDatabase.instance.packageStickerDataSourceFactory, 10)
            .setBoundaryCallback(object : PagedList.BoundaryCallback<StickerPackageModel>() {
                override fun onZeroItemsLoaded() {
                    super.onZeroItemsLoaded()
                }

                override fun onItemAtEndLoaded(itemAtEnd: StickerPackageModel) {
                    super.onItemAtEndLoaded(itemAtEnd)
                }

                override fun onItemAtFrontLoaded(itemAtFront: StickerPackageModel) {
                    super.onItemAtFrontLoaded(itemAtFront)
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
