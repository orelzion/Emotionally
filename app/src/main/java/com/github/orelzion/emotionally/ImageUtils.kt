package com.github.orelzion.emotionally

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


fun File.getBitmapInSize(maxHeightInPx: Int, maxWidthInPx: Int): Bitmap? {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    Timber.d("Original bitmap resolution is %sx%s", options.outWidth, options.outHeight)
    Timber.d("Original file size is %s", this.totalSpace)

    var scale = 1
    if (options.outWidth > maxWidthInPx || options.outHeight > maxHeightInPx) {
        while (options.outWidth / scale >= maxWidthInPx &&
            options.outHeight / scale >= maxHeightInPx
        ) {
            scale *= 2
        }
    }

    Timber.d("New bitmap scael should be %s", scale)

    val newOptions = BitmapFactory.Options()
    newOptions.inSampleSize = scale
    val scaledBitmap = BitmapFactory.decodeStream(FileInputStream(this), null, newOptions)

    Timber.d("New bitmap resolution us %sx%s", newOptions.outHeight, newOptions.outWidth)
    Timber.d("New file size is %s", this.totalSpace)

    return rotateImage(scaledBitmap, path)
}

fun Bitmap.createTempFile(): File {
    val newFile = File(EmotionallyApplication.INSTANCE.cacheDir, "temp.jpeg")
    Timber.d("new file path %s", newFile.path)
    val fileOutputStream = FileOutputStream(newFile.path)
    compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
    return newFile
}

fun rotateImage(bitmap: Bitmap?, path: String): Bitmap? {
    var rotate = 0f
    val exif = ExifInterface(path)
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270F
        ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180F
        ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90F
    }
    val matrix = Matrix().apply {
        postRotate(rotate)
    }

    return bitmap?.run {
        Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width,
            bitmap.height, matrix, true
        )
    }
}