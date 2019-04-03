package com.pacific.barcode

import android.content.Context
import android.graphics.Point
import android.hardware.Camera
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers
import java.io.IOException

/**
 * This class is for android targets below android 5.0 and it uses old camera api
 */
class CameraManager(context: Context) : BaseCameraManager(context), Camera.AutoFocusCallback, Camera.PreviewCallback {

    private var camera: Camera? = null

    override fun onAutoFocus(success: Boolean, camera: Camera) {
        if (hook || isRelease) return
        camera.setOneShotPreviewCallback(this)
    }

    override fun connectCamera(surfaceHolder: SurfaceHolder) {
        if (!isRelease) return
        try {
            camera = Camera.open()
            isRelease = false
            camera!!.setPreviewDisplay(surfaceHolder)
            setCameraParameter()
            camera!!.startPreview()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    override fun releaseCamera() {
        if (isRelease) return
        isRelease = true
        camera!!.cancelAutoFocus()
        camera!!.stopPreview()
        try {
            camera!!.setPreviewDisplay(null)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        camera!!.release()
        camera = null
    }

    override fun startCapture() {
        if (hook || isRelease || executor.isShutdown) return
        executor.execute { camera!!.autoFocus(this@CameraManager) }
    }

    override fun setCameraParameter() {
        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(0, cameraInfo)
        var degrees = 0
        when (rotate) {
            Surface.ROTATION_0 -> {
                degrees = 0
            }
            Surface.ROTATION_90 -> {
                degrees = 90
            }
            Surface.ROTATION_180 -> {
                degrees = 180
            }
            Surface.ROTATION_270 -> {
                degrees = 270
            }
        }

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (cameraInfo.orientation + degrees) % 360
            displayOrientation = (360 - displayOrientation) % 360
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360
        }

        /** Warning : may throw exception with parameters not supported  */
        val parameters = camera!!.parameters
        val previewSizes = parameters.supportedPreviewSizes
        var bestSize: Camera.Size = previewSizes[0]
        for (i in 1 until previewSizes.size) {
            if (previewSizes[i].width * previewSizes[i].height > bestSize.width * bestSize.height) {
                bestSize = previewSizes[i]
            }
        }
        parameters.setPreviewSize(bestSize.width, bestSize.height)

        val pictureSizes = parameters.supportedPictureSizes
        bestSize = pictureSizes[0]
        for (i in 1 until pictureSizes.size) {
            if (pictureSizes[i].width * pictureSizes[i].height > bestSize.width * bestSize.height) {
                bestSize = pictureSizes[i]
            }
        }
        parameters.setPictureSize(bestSize.width, bestSize.height)
        camera!!.parameters = parameters
        camera!!.setDisplayOrientation(displayOrientation)
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        if (hook || executor.isShutdown) return
        Observable
                .just<Camera.Size>(camera.parameters.previewSize)
                .subscribeOn(Schedulers.from(executor))
                .map { size -> getCodeValue(data, Point(size.width, size.height)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Action1 { qrResult ->
                    if (qrResult == null) {
                        count++
                        startCapture()
                        return@Action1
                    }
                    QRUtils.vibrate(context)
                    if (onResultListener != null) {
                        onResultListener.onResult(qrResult)
                    }
                    count = 0
                }, Action1 { Log.e("CameraManager", "getCodeValue() failed .") })
    }
}
