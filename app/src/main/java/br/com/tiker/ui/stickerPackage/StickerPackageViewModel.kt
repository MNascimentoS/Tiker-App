package br.com.tiker.ui.stickerPackage

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiker.R
import br.com.tiker.model.Constants
import br.com.tiker.model.Sticker
import br.com.tiker.model.StickerPack
import br.com.tiker.persistence.SaveBitmapToDevice
import br.com.tiker.persistence.StickerRoomDatabase
import br.com.tiker.persistence.model.StickerIdentifierEntity
import br.com.tiker.services.StickerContentProvider
import br.com.tiker.utils.FileUtils
import br.com.tiker.utils.StickerPackValidator
import br.com.tiker.utils.StickerPacksContainer
import br.com.tiker.utils.StickerPacksManager
import com.google.gson.Gson
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.ByteArrayOutputStream
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random


class StickerPackageViewModel : ViewModel(), KoinComponent {

    private val stickerDb: StickerRoomDatabase by inject()
    private val saveBitmapToDevice: SaveBitmapToDevice by inject()

    lateinit var stickerPack: StickerPack
    private var stickerPackageId = 0
    var stickerPackAdded: Boolean? = null

    val uiEventLiveData = MutableLiveData<Pair<Int, Any>>()

    var isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var closeActivity: MutableLiveData<Boolean> = MutableLiveData()

    var identifier = ""
    var namePackage: MutableLiveData<String> = MutableLiveData()
    var authorPackage: MutableLiveData<String> = MutableLiveData()
    var stickerList: MutableLiveData<List<Bitmap>> = MutableLiveData()

    private var _createdPackage: MutableLiveData<Boolean> = MutableLiveData()
    var createdPackage: LiveData<Boolean> = _createdPackage

    var error: String? = null

    fun loadSavedStickerList(id: Int) {
        isLoading.value = true
        viewModelScope.launch {
            val result = stickerDb.stickerDao().getLastStickerListAllData(id)
            if (result.first != null && result.second != null) {
                stickerPackageId = result.first!!.id
                val bitmapList = arrayListOf<Bitmap>()
                result.second!!.forEach {
                    val bmp = BitmapFactory.decodeByteArray(it.sticker, 0, it.sticker.size)
                    bitmapList.add(bmp)
                }
                namePackage.value = result.first?.name ?: "-"
                authorPackage.value = result.first?.author ?: "-"
                stickerList.value = bitmapList
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
        viewModelScope.launch {
            identifier = random() + Random.nextInt()

            stickerPack = StickerPack(identifier, namePackage.value, context.getString(R.string.app_name), "", "tickerapp0@gmail.com", "", "", "")

            val stickerUriList = arrayListOf<Uri>()
            stickerList.value?.forEach {
                getImageUri(it, context.contentResolver)?.let { uri ->
                    stickerUriList.add(uri)
                }
            }

            //Save the sticker images locally and get the list of new stickers for pack
            val stickerPath: String = Constants.STICKERS_DIRECTORY_PATH + identifier
            val stickerList: List<Sticker?> = StickerPacksManager.saveStickerPackFilesLocally(identifier, stickerUriList, context)
            stickerPack.stickers = stickerList


            //Generate image tray icon
            val trayIconFile = FileUtils.generateRandomIdentifier() + ".png"
            StickerPacksManager.createStickerPackTrayIconFile(stickerUriList[0], Uri.parse("$stickerPath/$trayIconFile"), context)

            stickerPack.trayImageFile = trayIconFile

            //Save stickerPack created to write in json
            StickerPacksManager.stickerPacksContainer = StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(context))
            StickerPacksManager.stickerPacksContainer.addStickerPack(stickerPack)
            StickerPacksManager.saveStickerPacksToJson(StickerPacksManager.stickerPacksContainer)
            insertStickerPackInContentProvider(stickerPack, context)

            try {
                StickerPackValidator.verifyStickerPackValidity(context, stickerPack)
                stickerPackAdded = true
            } catch (ex: Exception) {
                error = ex.message
                Sentry.capture(ex)
                stickerPackAdded = false
            }
            stickerDb.stickerDao().addStickerIdentifier(StickerIdentifierEntity(identifier))

            _createdPackage.value = true
            isLoading.value = false
        }
    }

    fun clearStickersFromFileSystem(context: Context) = viewModelScope.launch {
        try {
            saveBitmapToDevice.clearStoredFiles(context.contentResolver)
            try {
                StickerPacksManager.deleteStickerPack(identifier)
                stickerDb.stickerDao().getAllStickerIdentifier()?.forEach {
                    StickerPacksManager.deleteStickerPack(it?.identifier)
                }
            } catch (ex: Exception) { }
            stickerDb.stickerDao().removeAllStickerIdentifier()
        } catch (ex: Exception) { }
    }

    private fun getImageUri(bitmap: Bitmap, contentResolver: ContentResolver): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)

        val path = saveBitmapToDevice.insertImageIntoGallery(contentResolver, bitmap, random() + Random.nextInt(), identifier)
        return Uri.parse(path)
    }

    private fun insertStickerPackInContentProvider(stickerPack: StickerPack, context: Context) {
        val contentValues = ContentValues()
        contentValues.put("stickerPack", Gson().toJson(stickerPack))
        context.contentResolver.insert(StickerContentProvider.AUTHORITY_URI, contentValues)
    }

    private fun random(): String? {
        val generator = java.util.Random()
        val randomStringBuilder = StringBuilder()
        val randomLength = generator.nextInt(20)
        var tempChar: Char
        for (i in 0 until randomLength) {
            tempChar = (generator.nextInt(42) + 48).toChar()
            randomStringBuilder.append(tempChar)
        }
        var string = randomStringBuilder.toString()

        string = string.replace("/", "")
        string = string.replace(":", "")
        string = string.replace("-", "")
        string = string.replace(";", "")
        string = string.replace(">", "")
        string = string.replace("<", "")
        string = string.replace("=", "")
        string = string.replace("@", "")
        string = string.replace("?", "")
        return string
    }

    companion object {
        const val ADD_PACK = 200

        const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
        const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
        const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"
    }

}
