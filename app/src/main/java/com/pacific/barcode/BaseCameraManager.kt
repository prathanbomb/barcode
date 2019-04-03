package com.pacific.barcode

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.SurfaceHolder
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class BaseCameraManager(protected var context: Context) {
    private val qrBoxSize: Point

    protected var hook = false
    protected var rotate: Int = 0
    protected var count = 0
    protected var isRelease = true
    var executor: ExecutorService
        protected set
    protected var displayOrientation: Int = 0
    private var reader: MultiFormatReader
    protected lateinit var onResultListener: OnResultListener

    init {
        executor = Executors.newSingleThreadExecutor()
        reader = MultiFormatReader()
        qrBoxSize = Point()
        qrBoxSize.x = context.resources.getDimension(R.dimen.width_qr_box_view).toInt()
        qrBoxSize.y = context.resources.getDimension(R.dimen.height_qr_box_view).toInt()
    }

    protected fun getCodeValue(data: ByteArray, previewSize: Point): QRResult? {
        var bitmap: Bitmap? = null
        val stream = ByteArrayOutputStream(data.size)
        val image = YuvImage(data, ImageFormat.NV21, previewSize.x, previewSize.y, null)
        val left = previewSize.x - qrBoxSize.x shr 1
        val right = previewSize.x + qrBoxSize.x shr 1
        val top = previewSize.y - qrBoxSize.y shr 1
        val bottom = previewSize.y + qrBoxSize.y shr 1
        val rect = Rect(left, top, right, bottom)
        if (image.compressToJpeg(rect, 100, stream)) {
            val bytes = stream.toByteArray()
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        try {
            stream.close()
        } catch (e: IOException) {
            Log.e("onPreviewFrame", e.toString())
        }

        if (displayOrientation > 0) {
            val matrix = Matrix()
            matrix.postRotate(displayOrientation.toFloat())
            val newBitmap = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, false)
            bitmap.recycle()
            bitmap = newBitmap
        }

        val width = bitmap!!.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val result = QRUtils.decode(RGBLuminanceSource(width, height, pixels), reader)
        if (result != null) {
            return QRResult(bitmap, result)
        } else {
            bitmap.recycle()
            return null
        }
    }

    fun shutdownExecutor() {
        executor.shutdown()
    }

    abstract fun connectCamera(surfaceHolder: SurfaceHolder)

    abstract fun setCameraParameter()

    abstract fun startCapture()

    abstract fun releaseCamera()

    interface OnResultListener {
        fun onResult(qrResult: QRResult)
    }

    companion object {
        fun setRotate(baseCameraManager: BaseCameraManager, rotate: Int) {
            baseCameraManager.rotate = rotate
        }

        fun setHook(baseCameraManager: BaseCameraManager, hook: Boolean) {
            baseCameraManager.hook = hook
        }

        fun setOnResultListener(baseCameraManager: BaseCameraManager, onResultListener: OnResultListener) {
            baseCameraManager.onResultListener = onResultListener
        }
    }
}
