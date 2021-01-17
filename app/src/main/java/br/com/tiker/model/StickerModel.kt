package br.com.tiker.model

import android.graphics.Bitmap

data class StickerModel(
    var id: Int,
    var image: Bitmap? = null,
    var filePath: String = "",
    var selected: Boolean = false
)