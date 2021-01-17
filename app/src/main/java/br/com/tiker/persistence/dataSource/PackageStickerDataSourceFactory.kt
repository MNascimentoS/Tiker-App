package br.com.tiker.persistence.dataSource

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import br.com.tiker.model.StickerPackageModel


class PackageStickerDataSourceFactory : DataSource.Factory<Int, StickerPackageModel>() {

    private var stickerDataSource: PackageStickerDataSource? = null
    private var mutableLiveData: MutableLiveData<PackageStickerDataSource>? = null

    init {
        mutableLiveData = MutableLiveData()
    }

    override fun create(): DataSource<Int, StickerPackageModel> {
        stickerDataSource = PackageStickerDataSource()
        mutableLiveData?.postValue(stickerDataSource)
        return stickerDataSource as PackageStickerDataSource
    }

}