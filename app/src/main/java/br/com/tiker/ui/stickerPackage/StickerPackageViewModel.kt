package br.com.tiker.ui.stickerPackage

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiker.R
import br.com.tiker.model.Constants
import br.com.tiker.model.Sticker
import br.com.tiker.model.StickerModel
import br.com.tiker.model.StickerPack
import br.com.tiker.persistence.SaveBitmapToDevice
import br.com.tiker.persistence.StickerRoomDatabase
import br.com.tiker.persistence.model.StickerIdentifierEntity
import br.com.tiker.services.StickerContentProvider
import br.com.tiker.utils.*
import com.google.gson.Gson
import io.sentry.Sentry
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.random.Random


class StickerPackageViewModel : ViewModel(), KoinComponent {

    private val stickerDb: StickerRoomDatabase by inject()
    private val saveBitmapToDevice: SaveBitmapToDevice by inject()

    var stickerPack = arrayListOf<StickerPack>()
    private var stickerPackageId = 0
    var stickerPackAdded: Boolean? = null

    val uiEventLiveData = MutableLiveData<Pair<Int, Any>>()

    var isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var closeActivity: MutableLiveData<Boolean> = MutableLiveData()

    var identifier = ""
    var namePackage: MutableLiveData<String> = MutableLiveData()
    var authorPackage: MutableLiveData<String> = MutableLiveData()
    private var _stickerList: MutableLiveData<List<StickerModel>> = MutableLiveData()
    var stickerList: LiveData<List<StickerModel>> = _stickerList
    var fileName: ArrayList<String> = arrayListOf()

    var maxStickerSize = false
    val stickerPackageIdentifierList = arrayListOf<String>()
    val stickerPackageNameList = arrayListOf<String>()

    private var _createdPackage: MutableLiveData<Boolean> = MutableLiveData()
    var createdPackage: LiveData<Boolean> = _createdPackage

    var error: String? = null

    fun loadSavedStickerList(id: Int) {
        isLoading.value = true
        viewModelScope.launch {
            val result = stickerDb.stickerDao().getLastStickerListAllData(id)
            if (result.first != null && result.second != null) {
                stickerPackageId = result.first!!.id
                identifier = result.first!!.identifier ?: ""
                val list = arrayListOf<StickerModel>()
                result.second!!.forEachIndexed { index, value ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && value.stickerFile != null) {
                        list.add(
                            StickerModel(
                                index,
                                BitmapFactory.decodeFile(value.stickerFile),
                                ImageDecoder.decodeDrawable(ImageDecoder.createSource(File(value.stickerFile!!))),
                                filePath = value.stickerFile ?: ""
                            )
                        )
                    } else {
                        list.add(
                            StickerModel(
                                index,
                                BitmapFactory.decodeByteArray(value.sticker, 0, value.sticker.size),
                                filePath = value.stickerFile ?: ""
                            )
                        )
                    }
                }
                namePackage.value = result.first?.name ?: "-"
                authorPackage.value = result.first?.author ?: "-"
                _stickerList.value = list
            }
            isLoading.value = false
        }
    }

    fun removeStickerPackage(id: Int) {
        isLoading.value = true
        viewModelScope.launch {
            stickerDb.stickerDao().removeSavedStickersPackageList(id)
            isLoading.value = false
            closeActivity.value = true
        }
    }

    fun createPackageToWhatsApp(context: Context) {
        isLoading.value = true

        maxStickerSize = false
        stickerPack.clear()
        stickerPackageIdentifierList.clear()
        stickerPackageNameList.clear()

        viewModelScope.launch {
            if (identifier.isBlank()) {
                identifier = random() + Random.nextInt()
                stickerDb.stickerDao().saveLastUnsavedStickerList(stickerPackageId, identifier, namePackage.value
                        ?: "", authorPackage.value ?: "")
            }

            var stickerPackageUriList = arrayListOf<List<Uri>>()
            if (stickerList.value?.size ?: 0 > StickerPackValidator.STICKER_SIZE_MAX) {
                maxStickerSize = true

                val stickerUriList = arrayListOf<Uri>()
                var count = 1
                stickerList.value?.forEachIndexed { index, value ->
                    if (index % StickerPackValidator.STICKER_SIZE_MAX == 0) {
                        stickerPackageIdentifierList.add(random() + Random.nextInt())
                        stickerPackageNameList.add(namePackage.value + " " + count)
                        count++
                    }
                    getImageUri(value, context.contentResolver, stickerPackageIdentifierList.last())?.let { uri ->
                        stickerUriList.add(uri)
                    }
                }
                stickerPackageNameList.reverse()

                stickerPackageUriList = chopped(stickerUriList, StickerPackValidator.STICKER_SIZE_MAX) ?: arrayListOf()
            } else {
                val stickerUriList = arrayListOf<Uri>()
                stickerList.value?.forEach {
                    getImageUri(it, context.contentResolver, identifier)?.let { uri ->
                        stickerUriList.add(uri)
                        fileName.add(File(it.filePath).name)
                    }
                }

                stickerPackageUriList.add(stickerUriList)
                stickerPackageIdentifierList.add(identifier)
                stickerPackageNameList.add(namePackage.value ?: "")
            }

            stickerPackageUriList.forEachIndexed { index, uriList ->
                stickerPack.add(StickerPack(
                        stickerPackageIdentifierList[index],
                        stickerPackageNameList[index],
                        context.getString(R.string.app_name),
                        "",
                        "tickerapp0@gmail.com",
                        "",
                        "",
                        ""))

                //Save the sticker images locally and get the list of new stickers for pack
                val stickerPath: String = Constants.STICKERS_DIRECTORY_PATH + stickerPackageIdentifierList[index]
                val stickerList: List<Sticker?> = StickerPacksManager.saveStickerPackFilesLocally(stickerPackageIdentifierList[index], uriList, context)
                stickerPack.last().stickers = stickerList

                //Generate image tray icon
                val trayIconFile = FileUtils.generateRandomIdentifier() + ".png"
                StickerPacksManager.createStickerPackTrayIconFile(uriList[0], Uri.parse("$stickerPath/$trayIconFile"), context)
                stickerPack.last().trayImageFile = trayIconFile

                //Save stickerPack created to write in json
                StickerPacksManager.stickerPacksContainer = StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(context))
                StickerPacksManager.stickerPacksContainer.addStickerPack(stickerPack.last())
                StickerPacksManager.saveStickerPacksToJson(StickerPacksManager.stickerPacksContainer)
                insertStickerPackInContentProvider(stickerPack.last(), context)
                try {
                    StickerPackValidator.verifyStickerPackValidity(context, stickerPack.last())
                    stickerPackAdded = true
                } catch (ex: Exception) {
                    error = ex.message
                    Sentry.capture(ex)
                    stickerPackAdded = false
                }
            }

            stickerPackageIdentifierList.forEach {
                try {
                    stickerDb.stickerDao().addStickerIdentifier(StickerIdentifierEntity(it))
                } catch (ex: java.lang.Exception) { }
            }
            _createdPackage.value = true
            isLoading.value = false
        }
    }

    fun clearStickersFromFileSystem(context: Context) = viewModelScope.launch {
        try {
            saveBitmapToDevice.clearStoredFiles(context.contentResolver)
            try {
                stickerDb.stickerDao().getAllStickerIdentifier()?.forEach {
                    StickerPacksManager.deleteStickerPack(it?.identifier)
                }
            } catch (ex: Exception) {
            }
            stickerDb.stickerDao().removeAllStickerIdentifier()
        } catch (ex: Exception) {
        }
    }

    private fun getImageUri(sticker: StickerModel, contentResolver: ContentResolver, identifier: String): Uri? {
        val path = saveBitmapToDevice.insertImageIntoGallery(contentResolver, sticker, random() + Random.nextInt(), identifier)
        return Uri.parse(path)
    }

    private fun insertStickerPackInContentProvider(stickerPack: StickerPack, context: Context) {
        val contentValues = ContentValues()
        contentValues.put("stickerPack", Gson().toJson(stickerPack))
        context.contentResolver.insert(StickerContentProvider.AUTHORITY_URI, contentValues)
    }

    companion object {
        const val ADD_PACK = 200

        const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
        const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
        const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"
    }

}
