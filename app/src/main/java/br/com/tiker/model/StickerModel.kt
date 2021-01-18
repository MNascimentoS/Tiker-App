package br.com.tiker.model

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class StickerModel(
    var id: Int,
    var image: Bitmap,
    var drawable: Drawable? = null,
    var filePath: String = "",
    var selected: Boolean = false
)