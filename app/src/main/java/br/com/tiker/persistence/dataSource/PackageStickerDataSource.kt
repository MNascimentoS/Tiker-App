package br.com.tiker.persistence.dataSource

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.paging.PageKeyedDataSource
import br.com.tiker.model.StickerModel
import br.com.tiker.model.StickerPackageModel
import br.com.tiker.persistence.StickerRoomDatabase
import br.com.tiker.persistence.model.StickerPackageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File
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
            val stickerPackageList = getStickerPacks(params.requestedLoadSize)
            callback.onResult(stickerPackageList, null, if (stickerPackageList.size > 0) newPage else 0)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, StickerPackageModel>) {
        launch {
            val stickerPackageList = getStickerPacks(params.requestedLoadSize)
            callback.onResult(stickerPackageList, newPage)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, StickerPackageModel>) {}

    private suspend fun getStickerPacks(requestedLoadSize: Int) : ArrayList<StickerPackageModel> {
        val packageResult = arrayListOf<StickerPackageModel>()
        newPage++
        resultList = stickerDb.stickerDao().getSavedStickerList()
        resultList?.let { stickerPackageList ->
            repeat(requestedLoadSize) {
                if (stickerPackageList.size > currentValue) {
                    val sPackage = stickerPackageList[currentValue]
                    val stickerDBList = stickerDb.stickerDao().getLastStickerListAllData(sPackage.id)
                    val stickerList = arrayListOf<StickerModel>()

                    stickerDBList.second?.let { stickers ->
                        for (i in 0..4) {
                            if (stickers.size > i) {
                                val sticker = stickers[i]
//                                val bmp = BitmapFactory.decodeByteArray(sticker.sticker, 0, sticker.sticker.size)
//                                stickerList.add(
//                                        StickerModel(
//                                                id = sticker.id,
//                                                selected = false,
//                                                image = bmp
//                                        )
//                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !sticker.stickerFile.isNullOrEmpty()) {
                                    stickerList.add(
                                        StickerModel(
                                            sticker.id,
                                            BitmapFactory.decodeFile(sticker.stickerFile!!),
                                            ImageDecoder.decodeDrawable(ImageDecoder.createSource(
                                                File(sticker.stickerFile!!)
                                            )),
                                            filePath = sticker.stickerFile ?: ""
                                        )
                                    )
                                } else {
                                    stickerList.add(
                                        StickerModel(
                                            sticker.id,
                                            BitmapFactory.decodeByteArray(sticker.sticker, 0, sticker.sticker.size),
                                            filePath = sticker.stickerFile ?: ""
                                        )
                                    )
                                }
                            }
                        }
                    }

                    packageResult.add(StickerPackageModel(
                            id = sPackage.id,
                            name = sPackage.name,
                            author = sPackage.author,
                            stickerList = stickerList
                    ))
                    currentValue++
                }
            }
        }
        return packageResult
    }

}