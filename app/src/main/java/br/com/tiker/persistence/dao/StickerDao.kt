package br.com.tiker.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import br.com.tiker.persistence.model.StickerEntity
import br.com.tiker.persistence.model.StickerPackageEntity


@Dao
interface StickerDao {

    /* INSERT */
    @Insert
    suspend fun addSticker(stickerEntity: List<StickerEntity>) : List<Long>

    @Insert
    suspend fun addStickerPackage(stickerPackageEntity: StickerPackageEntity)

    @Transaction
    suspend fun addStickerList(stickerPackageEntity: StickerPackageEntity, stickerEntity: List<StickerEntity>) {
        val stickerList = addSticker(stickerEntity)
        stickerPackageEntity.stickerList = stickerList
        addStickerPackage(stickerPackageEntity)
    }

    /* SELECT */

    @Query("SELECT * FROM sticker WHERE id = :id")
    suspend fun getSavedSticker(id: Int) : StickerEntity?

    @Query("SELECT * FROM sticker_list WHERE name != ''")
    suspend fun getSavedStickerList() : List<StickerPackageEntity>?

    @Query("SELECT * FROM sticker_list WHERE name = ''")
    suspend fun getUnsavedStickerList() : List<StickerPackageEntity>?

    @Query("SELECT * FROM sticker_list WHERE id = :id")
    suspend fun getSavedStickerList(id: Int) : StickerPackageEntity?

    @Query("SELECT * FROM sticker_list ORDER BY id DESC LIMIT 1")
    suspend fun getLastUnsavedStickerList(): StickerPackageEntity?

    @Transaction
    suspend fun getLastUnsavedStickerListAllData(): Pair<StickerPackageEntity?, List<StickerEntity>?> {
        val stickerPackageEntity = getLastUnsavedStickerList()
        val stickerEntityList = arrayListOf<StickerEntity>()
        stickerPackageEntity?.stickerList?.forEach { id ->
            getSavedSticker(id.toInt())?.let {
                stickerEntityList.add(it)
            }
        }
        return Pair(stickerPackageEntity, if (stickerEntityList.isNotEmpty()) stickerEntityList else null)
    }

    @Transaction
    suspend fun getLastStickerListAllData(id: Int): Pair<StickerPackageEntity?, List<StickerEntity>?> {
        val stickerPackageEntity = getSavedStickerList(id)
        val stickerEntityList = arrayListOf<StickerEntity>()
        stickerPackageEntity?.stickerList?.forEach { stickerId ->
            getSavedSticker(stickerId.toInt())?.let {
                stickerEntityList.add(it)
            }
        }
        return Pair(stickerPackageEntity, if (stickerEntityList.isNotEmpty()) stickerEntityList else null)
    }

    /* UPDATE */

    @Query("UPDATE sticker_list SET name = :name, author = :author WHERE id = :id")
    suspend fun saveLastUnsavedStickerList(id: Int, name: String, author: String)

    /* DELETE */

    @Query("DELETE FROM sticker_list WHERE name = ''")
    suspend fun removeUnsavedStickersPackageList()

    @Query("DELETE FROM sticker_list WHERE id = :id")
    suspend fun removeStickerPackage(id: Int)

    @Query("DELETE FROM sticker WHERE id = :id")
    suspend fun removeSticker(id: Int)

    @Transaction
    suspend fun removeAllUnsavedStickersPackageList() {
        val stickerPackageList = getUnsavedStickerList()
        stickerPackageList?.forEach {
            it.stickerList.forEach { sticker ->
                removeSticker(sticker.toInt())
            }
        }
        removeUnsavedStickersPackageList()
    }

    @Transaction
    suspend fun removeSavedStickersPackageList(id: Int) {
        val stickerPackageList = getSavedStickerList(id)
        stickerPackageList?.stickerList?.forEach { sticker ->
            removeSticker(sticker.toInt())
        }
        removeStickerPackage(id)
    }

}