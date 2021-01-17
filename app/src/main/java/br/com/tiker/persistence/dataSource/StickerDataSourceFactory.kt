package br.com.tiker.persistence.dataSource

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import br.com.tiker.model.StickerModel


class StickerDataSourceFactory : DataSource.Factory<Int, StickerModel>() {

    private var stickerDataSource: StickerDataSource? = null
    private var mutableLiveData: MutableLiveData<StickerDataSource>? = null

    init {
        mutableLiveData = MutableLiveData()
    }

    override fun create(): DataSource<Int, StickerModel> {
        stickerDataSource = StickerDataSource()
        mutableLiveData?.postValue(stickerDataSource)
        return stickerDataSource as StickerDataSource
    }

    fun getMutableLiveData(): MutableLiveData<StickerDataSource>? {
        return mutableLiveData
    }

}