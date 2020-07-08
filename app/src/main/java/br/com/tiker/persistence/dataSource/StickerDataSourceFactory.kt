package br.com.tiker.persistence.dataSource

import android.content.Context
import androidx.paging.DataSource
import androidx.lifecycle.MutableLiveData
import br.com.tiker.model.StickerModel


class StickerDataSourceFactory(private val context: Context) : DataSource.Factory<Int, StickerModel>() {

    private var stickerDataSource: StickerDataSource? = null
    private var mutableLiveData: MutableLiveData<StickerDataSource>? = null

    init {
        mutableLiveData = MutableLiveData()
    }

    override fun create(): DataSource<Int, StickerModel> {
        stickerDataSource = StickerDataSource(context)
        mutableLiveData?.postValue(stickerDataSource)
        return stickerDataSource as StickerDataSource
    }

}