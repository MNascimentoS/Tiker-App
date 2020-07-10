package br.com.tiker.persistence

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import br.com.tiker.model.Constants
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Will save our card (currently set as a bitmap) as a png and save it into our devices
 * native photo gallery. Unfortunately, the default method for this will add the image to the bottom
 * of the gallery by default, that is no good. This more complex method that utilizes Android's
 * native methods will allow us to store the bitmap at the top of the gallery by setting it's
 * meta data to today's date.
 */
class SaveBitmapToDevice  {

    /**
     * A copy of the Android internals insertImage method, this method populates the
     * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
     * that is inserted manually gets saved at the end of the gallery (because date is not populated).
     * @see android.provider.MediaStore.Images.Media.insertImage
     */
    fun insertImageIntoGallery(cr: ContentResolver, source: Bitmap?, title: String, packageName: String?): String? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, title)
        values.put(MediaStore.Images.Media.DESCRIPTION, "")
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }
        var url: Uri? = null
        var stringUrl: String? = null /* value to be returned */
        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (source != null) {
                val imageOut = cr.openOutputStream(url!!)
                imageOut.use { image ->
                    source.compress(Bitmap.CompressFormat.PNG, 100, image)
                }
                val id = ContentUris.parseId(url)
                // Wait until MINI_KIND thumbnail is generated.
                val miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null)
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id, 50f, 50f, MediaStore.Images.Thumbnails.MICRO_KIND)
            } else {
                //cr.delete(url!!, null, null)
                return storeToAlternateSd(source, title, packageName ?: "")
                // url = null;
            }
        } catch (e: Exception) {
            if (url != null) {
                //cr.delete(url, null, null)
                storeToAlternateSd(source, title, packageName ?: "")
                return url.toString()
                // url = null;
            }
        }
        if (url != null) {
            stringUrl = url.toString()
        }
        return stringUrl
    }

    /**
     * A copy of the Android internals StoreThumbnail method, it used with the insertImage to
     * populate the android.provider.MediaStore.Images.Media#insertImage with all the correct
     * meta data. The StoreThumbnail method is private so it must be duplicated here.
     * @see android.provider.MediaStore.Images.Media
     */
    private fun storeThumbnail(
            cr: ContentResolver,
            source: Bitmap,
            id: Long,
            width: Float,
            height: Float,
            kind: Int): Bitmap? {

        // create the matrix to scale it
        val matrix = Matrix()
        val scaleX = width / source.width
        val scaleY = height / source.height
        matrix.setScale(scaleX, scaleY)
        val thumb = Bitmap.createBitmap(source, 0, 0,
                source.width,
                source.height, matrix,
                true
        )
        val values = ContentValues(4)
        values.put(MediaStore.Images.Thumbnails.KIND, kind)
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID, id.toInt())
        values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.height)
        values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.width)
        val url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values)
        return try {
            val thumbOut = cr.openOutputStream(url!!)
            thumb.compress(Bitmap.CompressFormat.PNG, 100, thumbOut)
            thumbOut!!.close()
            thumb
        } catch (ex: FileNotFoundException) {
            Log.e("IMAGE_COMPRESSION_ERROR", "File not found")
            ex.printStackTrace()
            null
        } catch (ex: IOException) {
            Log.e("IMAGE_COMPRESSION_ERROR", "IO Exception")
            ex.printStackTrace()
            null
        }
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

}