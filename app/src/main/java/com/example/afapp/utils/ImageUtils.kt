package com.example.afapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

object ImageUtils {

    fun createTempImageUri(context: Context): Uri {
        val tempImagesDir = File(context.cacheDir, "temp_images")
        tempImagesDir.mkdirs()
        val tempFile = File.createTempFile("temp_image_", ".jpg", tempImagesDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    fun copyDrawableToInternalStorage(context: Context, @DrawableRes drawableId: Int, fileName: String): String? {
        return try {
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            val destinationFile = File(imagesDir, fileName)

            val drawable = ContextCompat.getDrawable(context, drawableId)
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                FileOutputStream(destinationFile).use { output ->
                    val format = if (fileName.endsWith(".png", true)) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                    bitmap.compress(format, 90, output)
                }
            } else {
                throw IOException("El drawable no es un Bitmap y no se puede guardar.")
            }

            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val fileName = "img_${UUID.randomUUID()}.jpg"
            val destinationFile = File(imagesDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            destinationFile.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getFileUri(filePath: String): Uri {
        return Uri.fromFile(File(filePath))
    }
}