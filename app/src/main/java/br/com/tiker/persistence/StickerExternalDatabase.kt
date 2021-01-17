package br.com.tiker.persistence

import br.com.tiker.persistence.dataSource.PackageStickerDataSourceFactory
import br.com.tiker.persistence.dataSource.StickerDataSourceFactory

class StickerExternalDatabase private constructor(
        val stickerDataSourceFactory: StickerDataSourceFactory,
        val packageStickerDataSourceFactory: PackageStickerDataSourceFactory
) {

    companion object {
        lateinit var instance: StickerExternalDatabase
            private set

        fun initialize() {
            instance = StickerExternalDatabase(
                StickerDataSourceFactory(),
                PackageStickerDataSourceFactory()
            )
        }
    }
}