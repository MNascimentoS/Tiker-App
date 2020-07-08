package br.com.tiker.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.tiker.persistence.converter.StickerListConverter
import br.com.tiker.persistence.dao.StickerDao
import br.com.tiker.persistence.model.StickerEntity
import br.com.tiker.persistence.model.StickerPackageEntity

@Database(
    version = StickerRoomDatabase.VERSION,
    exportSchema = false,
    entities = [StickerEntity::class, StickerPackageEntity::class]
)
@TypeConverters(StickerListConverter::class)
abstract class StickerRoomDatabase : RoomDatabase() {

    companion object {
        const val NAME = "sticker-db"
        const val VERSION = 1
    }

    abstract fun stickerDao(): StickerDao

}