package br.com.tiker.persistence.dataSource

import android.graphics.BitmapFactory
import android.os.Environment
import androidx.paging.PageKeyedDataSource
import br.com.tiker.model.StickerModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

class StickerDataSource() : PageKeyedDataSource<Int, StickerModel>(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var newPage = 0
    private var files = listOf<File>()
    private var lastFileIndex = 0

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, StickerModel>
    ) {
        newPage++
        launch {
            val stickers = getStickersFromWhatsApp(params.requestedLoadSize)
            callback.onResult(stickers, null, newPage)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, StickerModel>) {
        newPage++
        launch {
            val stickers = getStickersFromWhatsApp(params.requestedLoadSize)
            callback.onResult(stickers, newPage)
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

                val btm = BitmapFactory.decodeFile(files[lastFileIndex].absolutePath)
                btm?.let {
                    stickers.add(
                            StickerModel(
                                    lastFileIndex,
                                    BitmapFactory.decodeFile(files[lastFileIndex].absolutePath)
                            )
                    )
                }
                lastFileIndex++
            }
        }
        return stickers
    }

}