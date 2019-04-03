package com.pacific.barcode

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Vibrator
import android.util.Log
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer

/**
 * Created by UsherBaby on 2015/12/3.
 */
object QRUtils {

    /**
     * decode a image file.
     *
     * @param url    image file path
     * @param reader Z_X_ing MultiFormatReader
     */
    fun decode(url: String, reader: MultiFormatReader): QRResult? {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(url, options)
            when {
                options.outWidth >= 1920 -> options.inSampleSize = 6
                options.outWidth >= 1280 -> options.inSampleSize = 5
                options.outWidth >= 1024 -> options.inSampleSize = 4
                options.outWidth >= 960 -> options.inSampleSize = 3
            }
            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeFile(url) ?: return null
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val result = decode(RGBLuminanceSource(width, height, pixels), reader)
            if (result != null) {
                return QRResult(bitmap, result)
            }
            bitmap.recycle()
            return null
        } catch (e: Exception) {
            Log.e("decode exception", e.toString())
            return null
        }

    }

    /**
     * decode a LuminanceSource bitmap.
     *
     * @param source LuminanceSource bitmap
     * @param reader Z_X_ing MultiFormatReader
     */
    fun decode(source: LuminanceSource?, reader: MultiFormatReader): Result? {
        var result: Result? = null
        if (source != null) {
            val bBitmap = BinaryBitmap(HybridBinarizer(source))
            try {
                result = reader.decodeWithState(bBitmap)
            } catch (e: ReaderException) {
                result = null
            } finally {
                reader.reset()
            }
        }
        return result
    }

    fun vibrate(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(300)
    }
}
