package br.com.tiker.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.tiker.persistence.converter.StickerListConverter
import br.com.tiker.persistence.dao.StickerDao
import br.com.tiker.persistence.model.StickerEntity
import br.com.tiker.persistence.model.StickerIdentifierEntity
import br.com.tiker.persistence.model.StickerPackageEntity

@Database(
    version = StickerRoomDatabase.VERSION,
    exportSchema = false,
    entities = [StickerEntity::class, StickerPackageEntity::class, StickerIdentifierEntity::class]
)
@TypeConverters(StickerListConverter::class)
abstract class StickerRoomDatabase : RoomDatabase() {

    companion object {
        const val NAME = "sticker-db"
        const val VERSION = 2

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Begin SQL transaction
                database.execSQL("BEGIN TRANSACTION;")
                // Alter, create, drop, whatever
                database.execSQL("CREATE TABLE 'sticker-identifier' ('identifier' varchar(30) PRIMARY KEY NOT NULL);")
                // Commit transaction
                database.execSQL("COMMIT;")
            }
        }
    }

    abstract fun stickerDao(): StickerDao

}