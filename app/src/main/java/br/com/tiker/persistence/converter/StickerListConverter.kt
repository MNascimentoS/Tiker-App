package br.com.tiker.persistence.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StickerListConverter {

    @TypeConverter
    fun toStickerList(stickerListString: String?): List<Long>? {
        if (stickerListString == null) return null
        val type = object : TypeToken<List<Long>>() {}.type
        return Gson().fromJson(stickerListString, type)
    }

    @TypeConverter
    fun toJson(stickerListId: List<Long>?): String? {
        if (stickerListId == null) return null
        val type = object : TypeToken<List<Long>>() {}.type
        return Gson().toJson(stickerListId, type)
    }

}