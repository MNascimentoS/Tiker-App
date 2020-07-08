package br.com.tiker.persistence.dataSource

import androidx.paging.PageKeyedDataSource
import br.com.tiker.model.StickerPackageModel
import br.com.tiker.persistence.StickerRoomDatabase
import br.com.tiker.persistence.model.StickerPackageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

class PackageStickerDataSource : PageKeyedDataSource<Int, StickerPackageModel>(), KoinComponent, CoroutineScope {

    private val stickerDb: StickerRoomDatabase by inject()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var resultList: List<StickerPackageEntity>? = null
    private var newPage = 0
    private var currentValue = 0

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, StickerPackageModel>
    ) {
        launch {
            newPage++
            resultList = stickerDb.stickerDao().getSavedStickerList()
            resultList?.let { stickerPackageList ->
                val packageResult = arrayListOf<StickerPackageModel>()
                repeat(params.requestedLoadSize) {
                    if (stickerPackageList.size > currentValue) {
                        val sPackage = stickerPackageList[currentValue]
                        packageResult.add(StickerPackageModel(
                            id = sPackage.id,
                            name = sPackage.name,
                            author = sPackage.author,
                            stickerList = listOf()
                        ))
                        currentValue++
                    }
                }
                callback.onResult(packageResult, null, newPage)

            } ?: run {
                callback.onResult(arrayListOf(), null, 0)
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, StickerPackageModel>) {
        launch {
            newPage++
            resultList = stickerDb.stickerDao().getSavedStickerList()
            resultList?.let { stickerPackageList ->
                val packageResult = arrayListOf<StickerPackageModel>()
                repeat(params.requestedLoadSize) {
                    if (stickerPackageList.size < currentValue) {
                        currentValue++
                        val sPackage = stickerPackageList[currentValue]
                        packageResult.add(StickerPackageModel(
                            name = sPackage.name,
                            author = sPackage.author,
                            stickerList = listOf()
                        ))
                    }
                }
                callback.onResult(packageResult, newPage)

            } ?: run {
                callback.onResult(arrayListOf(), newPage)
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, StickerPackageModel>) {}

}