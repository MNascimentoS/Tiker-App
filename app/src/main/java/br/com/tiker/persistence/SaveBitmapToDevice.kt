package br.com.tiker.persistence

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimatedImageDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import br.com.tiker.model.Constants
import br.com.tiker.model.StickerModel
import com.facebook.animated.webp.WebPImage
import java.io.*
import java.nio.file.Files


/**
 * Will save our card (currently set as a bitmap) as a png and save it into our devices
 * native photo gallery. Unfortunately, the default method for this will add the image to the bottom
 * of the gallery by default, that is no good. This more complex method that utilizes Android's
 * native methods will allow us to store the bitmap at the top of the gallery by setting it's
 * meta data to today's date.
 */
class SaveBitmapToDevice {

    private val uriList = arrayListOf<Uri?>()

    /**
     * A copy of the Android internals insertImage method, this method populates the
     * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
     * that is inserted manually gets saved at the end of the gallery (because date is not populated).
     * @see android.provider.MediaStore.Images.Media.insertImage
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insertImageIntoGallery(
        context: Context,
        source: StickerModel,
        title: String,
        packageName: String?
    ): String? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, title)
        values.put(MediaStore.Images.Media.DESCRIPTION, "")
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title)
        values.put(
            MediaStore.Images.Media.MIME_TYPE,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && source.drawable is AnimatedImageDrawable) "image/gif"
            else "image/png"
        )

        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }
        var url: Uri? = null
        var stringUrl: String? = null
        try {
            url = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uriList.add(url)
            if (source.filePath.isNotBlank()) {
                //val webPImage = WebPImage.create(Files.readAllBytes( File(source.filePath).toPath()))
                return File(source.filePath).toURI().toString()
            } else {
                val imageOut = context.contentResolver.openOutputStream(url!!)
                imageOut.use { image ->
                    source.image.compress(Bitmap.CompressFormat.PNG, 100, image)
                }
            }
        } catch (e: Exception) {
            if (url != null) {
                storeToAlternateSd(source.image, title, packageName ?: "")
                return url.toString()
            }
        }
        if (url != null) {
            stringUrl = url.toString()
        }
        return stringUrl
    }

    /**
     * If we have issues saving into our MediaStore, save it directly to our SD card. We can then interact with this file
     * directly, opposed to pulling from the MediaStore. Again, this is a backup method if things don't work out as we
     * would expect (seeing as most devices will have a MediaStore).
     *
     * @param src
     * @param title
     * @return - the file's path
     */
    private fun storeToAlternateSd(src: Bitmap?, title: String, packageName: String): String? {
        if (src == null) return null
        val sdCardDirectory = File(Constants.STICKERS_DIRECTORY_PATH + packageName)
        if (!sdCardDirectory.exists()) sdCardDirectory.mkdir()
        val image = File(File(Constants.STICKERS_DIRECTORY_PATH + packageName), "$title.png")
        return try {
            val imageOut = FileOutputStream(image)
            src.compress(Bitmap.CompressFormat.PNG, 100, imageOut)
            imageOut.close()
            image.path
        } catch (ex: FileNotFoundException) {
            ex.printStackTrace()
            null
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    fun clearStoredFiles(contentResolver: ContentResolver) {
        uriList.forEach { uri ->
            uri?.let { contentResolver.delete(uri, null, null) }
        }
        uriList.clear()
    }
}