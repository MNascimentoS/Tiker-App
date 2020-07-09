package br.com.tiker.ui.stickerPackage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.tiker.BuildConfig
import br.com.tiker.R
import br.com.tiker.model.Constants
import br.com.tiker.model.Sticker
import br.com.tiker.model.StickerPack
import br.com.tiker.persistence.StickerRoomDatabase
import br.com.tiker.utils.StickerPackValidator
import br.com.tiker.utils.StickerPacksContainer
import br.com.tiker.services.StickerContentProvider
import br.com.tiker.utils.FileUtils
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


class StickerPackageViewModel : ViewModel(), KoinComponent, CoroutineScope {

    private val stickerDb: StickerRoomDatabase by inject()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    lateinit var stickerPack: StickerPack
    private var stickerPackageId = 0

    val uiEventLiveData = MutableLiveData<Pair<Int, Any>>()

    var isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var closeActivity: MutableLiveData<Boolean> = MutableLiveData()

    var namePackage: MutableLiveData<String> = MutableLiveData()
    var authorPackage: MutableLiveData<String> = MutableLiveData()
    var stickerList: MutableLiveData<List<Bitmap>> = MutableLiveData()

    fun loadSavedStickerList(id: Int){
        isLoading.value = true
        launch {
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
        launch {
            stickerDb.stickerDao().removeSavedStickersPackageList(id)
            isLoading.value = false
            closeActivity.value = true
        }
    }

    fun createPackageToWhatsApp(context: Context) {
        stickerPack = StickerPack(namePackage.value, namePackage.value, context.getString(R.string.app_name), "", "tickerapp0@gmail.com", "", "", "")

        val stickerUriList = arrayListOf<Uri>()
        stickerList.value?.forEach {
            getImageUri(it, context)?.let { uri ->
                stickerUriList.add(uri)
            }
        }

        //Save the sticker images locally and get the list of new stickers for pack
        val stickerPath: String = Constants.STICKERS_DIRECTORY_PATH + namePackage.value
        val stickerList: List<Sticker?> = StickerPacksManager.saveStickerPackFilesLocally(namePackage.value, stickerUriList, context)
        stickerPack.stickers = stickerList


        //Generate image tray icon
        val trayIconFile = FileUtils.generateRandomIdentifier() + ".png"
        StickerPacksManager.createStickerPackTrayIconFile(stickerUriList[0], Uri.parse("$stickerPath/$trayIconFile"), context)

        stickerPack.trayImageFile = trayIconFile

        //Save stickerPack created to write in json
        StickerPacksManager.stickerPacksContainer = StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(context))
        StickerPacksManager.deleteStickerPack(namePackage.value)
        StickerPacksManager.stickerPacksContainer.addStickerPack(stickerPack)
        StickerPacksManager.saveStickerPacksToJson(StickerPacksManager.stickerPacksContainer)
        insertStickerPackInContentProvider(stickerPack, context)

        try {
            StickerPackValidator.verifyStickerPackValidity(context, stickerPack)
        } catch (ex: Exception) {
            Sentry.capture(ex)
            // TODO Error
            //error = ex.message
        }

    }

    private fun getImageUri(bitmap: Bitmap, context: Context): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, random() + Random.nextInt(), null)
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
            tempChar = (generator.nextInt(96) + 32).toChar()
            randomStringBuilder.append(tempChar)
        }
        return randomStringBuilder.toString()
    }

    private fun createIntentToAddStickerPack(identifier: String, stickerPackName: String): Intent {
        val intent = Intent()
        intent.action = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
        intent.putExtra(EXTRA_STICKER_PACK_ID, identifier)
        intent.putExtra(
            EXTRA_STICKER_PACK_AUTHORITY,
            BuildConfig.CONTENT_PROVIDER_AUTHORITY
        )
        intent.putExtra(EXTRA_STICKER_PACK_NAME, stickerPackName)
        return intent
    }

    companion object {
        const val ADD_PACK = 200

        const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
        const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
        const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"
    }

}
