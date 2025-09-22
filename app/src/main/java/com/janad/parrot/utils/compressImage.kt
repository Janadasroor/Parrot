package com.janad.parrot.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun compressImageLegacy(
    context: Context,
    inputFile: File,
    quality: Int = 75,
    maxWidth: Int = 1080,
    maxHeight: Int = 1080
): File? {
    return try {
        // Read bitmap from file (works on all API levels)
        val inputStream: InputStream = inputFile.inputStream()
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Optional: resize before compressing
        val resizedBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)

        // Save compressed JPEG to cache
        val compressedFile = File(
            context.cacheDir,
            "compressed_image_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(compressedFile).use { out ->
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }

        compressedFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun resizeBitmap(
    original: Bitmap,
    maxWidth: Int,
    maxHeight: Int
): Bitmap {
    val width = original.width
    val height = original.height

    val scale = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height, 1.0f)
    val newWidth = (width * scale).toInt()
    val newHeight = (height * scale).toInt()

    return if (scale < 1.0f) {
        Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
    } else {
        original
    }
}
