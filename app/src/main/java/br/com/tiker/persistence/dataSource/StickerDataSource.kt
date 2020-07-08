package br.com.tiker.persistence.dataSource

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.paging.PageKeyedDataSource
import br.com.tiker.model.StickerModel
import io.cubos.r2d2lib.executeLongOperation
import java.io.File

class StickerDataSource(private val context: Context) : PageKeyedDataSource<Int, StickerModel>() {

    private var newPage = 0
    private var files = arrayOf<File>()
    private var lastFileIndex = 0

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, StickerModel>
    ) {
        executeLongOperation ({
            newPage++
            if (files.isEmpty()) {
                val path = Environment.getExternalStorageDirectory().toString() + "/WhatsApp/Media/WhatsApp Stickers"
                val directory = File(path)
                val allFiles = directory.listFiles()
                if (allFiles == null || allFiles.isEmpty()) {
                    return@executeLongOperation Pair(arrayListOf<StickerModel>(), 0)
                }
                files = allFiles
                files.reverse()
            }
            val stickers = mutableListOf<StickerModel>()
            run fillStickers@{
                repeat(params.requestedLoadSize) {
                    if (lastFileIndex > files.size) {
                        newPage = -1
                        return@fillStickers
                    }
                    stickers.add(
                        StickerModel(
                            lastFileIndex,
                            BitmapFactory.decodeFile(files[lastFileIndex].absolutePath)
                        )
                    )
                    lastFileIndex++
                }
            }
            Pair(stickers, newPage)
        }, { pair ->
            if (pair is Pair<*, *>) {
                if (pair.first is MutableList<*>) {
                    callback.onResult(pair.first as MutableList<StickerModel>, null, pair.second as Int)
                }
            }
        })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, StickerModel>) {
        executeLongOperation ({
            newPage++
            if (files.isEmpty()) {
                val path = Environment.getExternalStorageDirectory().toString() + "/WhatsApp/Media/WhatsApp Stickers"
                val directory = File(path)
                files = directory.listFiles()
                files.reverse()
            }
            val stickers = mutableListOf<StickerModel>()
            run fillStickers@{
                repeat(params.requestedLoadSize) {
                    if (lastFileIndex > files.size) {
                        newPage = -1
                        return@fillStickers
                    }
                    stickers.add(
                        StickerModel(
                            lastFileIndex,
                            BitmapFactory.decodeFile(files[lastFileIndex].absolutePath)
                        )
                    )
                    lastFileIndex++
                }
            }
            Pair(stickers, newPage)
        }, { pair ->
            if (pair is Pair<*, *>) {
                if (pair.first is MutableList<*>) {
                    callback.onResult(pair.first as MutableList<StickerModel>, newPage)
                }
            }
        })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, StickerModel>) {}

}