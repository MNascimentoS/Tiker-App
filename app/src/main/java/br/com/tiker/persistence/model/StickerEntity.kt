package br.com.tiker.persistence.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sticker")
data class StickerEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var sticker: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StickerEntity

        if (id != other.id) return false
        if (!sticker.contentEquals(other.sticker)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + sticker.contentHashCode()
        return result
    }
}