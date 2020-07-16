package br.com.tiker.persistence.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import br.com.tiker.persistence.converter.StickerListConverter

@Entity(tableName = "sticker_list")
data class StickerPackageEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var identifier: String? = null,
    var name: String = "",
    var author: String = "",
    @TypeConverters(StickerListConverter::class)
    var stickerList: List<Long> = listOf()
)