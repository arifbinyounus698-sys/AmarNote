package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ProfileMediaManager {
    private const val DIR_NAME = "profile_media"
    private const val AVATAR_FILE = "profile_avatar.jpg"
    private const val COVER_FILE = "profile_cover.jpg"

    private fun mediaDir(context: Context): File {
        return File(context.filesDir, DIR_NAME).apply { mkdirs() }
    }

    fun createTempCameraUri(context: Context): Uri {
        val camDir = File(context.cacheDir, "camera").apply { mkdirs() }
        val tempFile = File(camDir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    }

    fun loadBitmapFromUri(context: Context, uri: Uri, maxDim: Int = 1400): Bitmap? {
        return try {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, boundsOpts)
            }
            val srcW = boundsOpts.outWidth
            val srcH = boundsOpts.outHeight
            if (srcW <= 0 || srcH <= 0) return null

            var sampleSize = 1
            while (max(srcW / sampleSize, srcH / sampleSize) > maxDim) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun cropAndSavePhoto(
        context: Context,
        source: Bitmap,
        isAvatar: Boolean,
        zoomScale: Float,
        offsetXFraction: Float,
        offsetYFraction: Float
    ): String {
        val targetW = if (isAvatar) 512 else 1280
        val targetH = if (isAvatar) 512 else 720

        val output = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val baseScale = max(
            targetW.toFloat() / source.width.toFloat(),
            targetH.toFloat() / source.height.toFloat()
        )
        val effectiveScale = baseScale * zoomScale.coerceIn(1f, 4f)
        val drawnW = source.width * effectiveScale
        val drawnH = source.height * effectiveScale

        val maxDx = max(0f, (drawnW - targetW) / 2f)
        val maxDy = max(0f, (drawnH - targetH) / 2f)

        val tx = (targetW - drawnW) / 2f + (offsetXFraction.coerceIn(-1f, 1f) * maxDx)
        val ty = (targetH - drawnH) / 2f + (offsetYFraction.coerceIn(-1f, 1f) * maxDy)

        val matrix = Matrix().apply {
            postScale(effectiveScale, effectiveScale)
            postTranslate(tx, ty)
        }
        canvas.drawBitmap(source, matrix, null)

        val fileName = if (isAvatar) AVATAR_FILE else COVER_FILE
        val outFile = File(mediaDir(context), fileName)
        outFile.outputStream().use { out ->
            output.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return outFile.absolutePath
    }

    fun rotateBitmap90(source: Bitmap): Bitmap {
        val matrix = Matrix().apply { postRotate(90f) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun loadLocalBitmap(path: String): Bitmap? {
        if (path.isBlank()) return null
        val file = File(path)
        if (!file.exists()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (_: Exception) {
            null
        }
    }

    fun deletePhotoFile(path: String) {
        if (path.isBlank()) return
        runCatching { File(path).delete() }
    }

    fun encodeFileToBase64(path: String): String {
        if (path.isBlank()) return ""
        val file = File(path)
        if (!file.exists()) return ""
        return try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            ""
        }
    }

    fun decodeBase64ToFile(context: Context, base64: String, isAvatar: Boolean): String {
        if (base64.isBlank()) return ""
        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            val fileName = if (isAvatar) AVATAR_FILE else COVER_FILE
            val outFile = File(mediaDir(context), fileName)
            outFile.writeBytes(bytes)
            outFile.absolutePath
        } catch (_: Exception) {
            ""
        }
    }
}
