package br.com.tiker.model

data class StickerPackageModel(
    var id: Int = 0,
    var name: String,
    var author: String,
    val stickerList: List<StickerModel>
)