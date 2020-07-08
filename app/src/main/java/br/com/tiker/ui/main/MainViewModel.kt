package br.com.tiker.ui.main

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import br.com.tiker.persistence.StickerRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.*
import kotlin.coroutines.CoroutineContext


class MainViewModel : ViewModel(), KoinComponent, CoroutineScope {

    private val stickerDb: StickerRoomDatabase by inject()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun removeAllUnsavedStickers() = launch {
        stickerDb.stickerDao().removeAllUnsavedStickersPackageList()
    }

    fun retriveIntentData(activity: Activity, intent: Intent, contentResolver: ContentResolver) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
//            val toast = Toast.makeText(this, sharedText, Toast.LENGTH_LONG)
//            toast.show()
            // Update UI to reflect text being shared
        }
//        try {
//            val imageUris: ArrayList<Uri> = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
//            var cursor: Cursor? = null
//            var cursor2: Cursor? = null
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                cursor = contentResolver.query(imageUris[0], null, null, null)
//                cursor2 = contentResolver.query(imageUris[1], null, null, null)
//            }
//            cursor?.moveToFirst()
//            cursor2?.moveToFirst()
//            val str = cursor?.getString(0)?.replace("Conversa do WhatsApp com ", "")
//            val stickerList = imageUris.filter { it.toString().contains("webp") }
//
//            val stickerPack = StickerPack(
//                identifier = "0",//Random.nextInt(100, 10000).toString(),
//                name = str ?: "Sticker",
//                publisher = "Mateus",
//                trayImageFile = splitImageFile(stickerList.first().toString()),
//                publisherEmail = "mateus.nascimento@cubos.io",
//                publisherWebsite = "www.google.com",
//                privacyPolicyWebsite = "www.google.com",
//                licenseAgreementWebsite = "www.google.com",
//                imageDataVersion = "1",
//                avoidCache = true
//            )
//
//            stickerPack.setStickers(ArrayList(stickerList.map {
//                val filename = splitImageFile(it.toString())
//                savefile(it, filename, stickerPack.identifier)
//                Sticker(
//                    filename,
//                    listOf("☕", "🙂")
//                )
//            }))
//
//            PreferenceDataSource(activity).setStickerPack(stickerPack.identifier, stickerPack)
//
//            val intent = Intent()
//            intent.action = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
//            intent.putExtra(
//                "sticker_pack_id",
//                stickerPack.identifier
//            ) //identifier is the pack's identifier in contents.json file
//            intent.putExtra(
//                "sticker_pack_authority",
//                StickerContentProvider.AUTHORITY_URI
//            ) //authority is the ContentProvider's authority. In the case of the sample app it is BuildConfig.CONTENT_PROVIDER_AUTHORITY.
//            intent.putExtra(
//                "sticker_pack_name",
//                stickerPack.name
//            ) //stickerPackName is the name of the sticker pack.
//
//            try {
//                activity.startActivityForResult(intent, 200)
//            } catch (e: ActivityNotFoundException) {
////            Toast.makeText(this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show()
//            }
//        } catch (ex: Exception) {}
    }

    private fun splitImageFile(str: String) = "STK${(str.split("STK").last())}"

    private fun savefile(sourceuri: Uri, filename: String, identifier: String) {
        val sourceFilename = sourceuri.path
        val destinationFilename =
                Environment.getExternalStorageDirectory().path + File.separatorChar + identifier + File.separatorChar + filename
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            bis = BufferedInputStream(FileInputStream(sourceFilename))
            bos = BufferedOutputStream(FileOutputStream(destinationFilename, false))
            val buf = ByteArray(1024)
            bis.read(buf)
            do {
                bos.write(buf)
            } while (bis.read(buf) != -1)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                bis?.close()
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}
