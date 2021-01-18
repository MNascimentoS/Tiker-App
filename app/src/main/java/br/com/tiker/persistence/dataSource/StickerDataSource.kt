package br.com.tiker.persistence.dataSource

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import br.com.tiker.model.StickerModel
import io.cubos.r2d2lib.executeLongOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

class StickerDataSource: PageKeyedDataSource<Int, StickerModel>() {

    var loading = MutableLiveData<Boolean>()
    private var newPage = 0
    private var files = listOf<File>()
    private var lastFileIndex = 0

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, StickerModel>
    ) {
        newPage++
        loading.postValue(true)
        executeLongOperation ({
            val stickers = getStickersFromWhatsApp(params.requestedLoadSize)
            callback.onResult(stickers, null, newPage)
        }) {
            loading.postValue(false)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, StickerModel>) {
        newPage++
        loading.postValue(true)
        executeLongOperation ({
            val stickers = getStickersFromWhatsApp(params.requestedLoadSize)
            callback.onResult(stickers, newPage)
        }) {
            loading.postValue(false)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, StickerModel>) {}

    private fun getStickersFromWhatsApp(requestedLoadSize: Int) : MutableList<StickerModel> {
        if (files.isEmpty()) {
            val path = Environment.getExternalStorageDirectory().toString() + "/WhatsApp/Media/WhatsApp Stickers"
            val directory = File(path)
            val allFiles = directory.listFiles()?.toMutableList() ?: mutableListOf()

            files = allFiles.sortedByDescending { it.lastModified() }.toList()
        }

        val stickers = mutableListOf<StickerModel>()
        run fillStickers@{
            repeat(requestedLoadSize) {
                if (files.isEmpty() || lastFileIndex >= files.size) {
                    newPage = -1
                    return@fillStickers
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    stickers.add(
                            StickerModel(
                                    lastFileIndex,
                                    BitmapFactory.decodeFile(files[lastFileIndex].absolutePath),
                                    ImageDecoder.decodeDrawable(ImageDecoder.createSource(files[lastFileIndex])),
                                    filePath = files[lastFileIndex].absolutePath
                            )
                    )
                } else {
                    stickers.add(
                            StickerModel(
                                    lastFileIndex,
                                    BitmapFactory.decodeFile(files[lastFileIndex].absolutePath),
                                    filePath = files[lastFileIndex].absolutePath
                            )
                    )
                }
                lastFileIndex++
            }
        }
        return stickers
    }

}