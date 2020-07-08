package br.com.tiker.persistence


import android.content.Context
import android.preference.PreferenceManager
import br.com.tiker.model.StickerPack
import com.google.gson.Gson

class PreferenceDataSource(context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)!!

    fun setStickerPack(identifier: String, stickerPack: StickerPack) {
        sharedPreferences.edit().putString(identifier, Gson().toJson(stickerPack).toString()).apply()
    }

    fun getStickerPack(identifier: String): StickerPack? {
        val json = sharedPreferences.getString(identifier, null)
        return if (json != null) Gson().fromJson(json, StickerPack::class.java) else null
    }

    enum class PreferenceKeys {
        STICKER_PACK
    }
}
