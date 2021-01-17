package br.com.tiker.persistence.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sticker-identifier")
data class StickerIdentifierEntity(
    @PrimaryKey
    var identifier: String
)