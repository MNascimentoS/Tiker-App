package br.com.tiker.model

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import java.io.File

data class StickerModel(
    var id: Int,
    var image: Bitmap,
    var drawable: Drawable? = null,
    var filePath: String = "",
    var file: File? = null,
    var selected: Boolean = false
)