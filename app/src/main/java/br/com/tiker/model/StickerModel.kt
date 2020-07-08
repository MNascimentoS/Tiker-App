package br.com.tiker.model

import android.graphics.Bitmap

data class StickerModel(
    var id: Int,
    var image: Bitmap,
    var selected: Boolean = false
)