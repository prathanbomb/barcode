package com.pacific.barcode

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.SurfaceHolder
import com.google.zxing.MultiFormatReader
import com.pacific.mvc.Activity
import com.trello.rxlifecycle.ActivityEvent
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class QRActivity : Activity<QRModel>() {
    private var cameraManager: BaseCameraManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
        cameraManager = if (Build.VERSION_CODES.LOLLIPOP >= Build.VERSION.SDK_INT) {
            CameraManager(application)
        } else {
            CameraManager(application)
        }
        model = QRModel(QRView(this))
        model.onCreate()

        BaseCameraManager.setOnResultListener(cameraManager!!, object : BaseCameraManager.OnResultListener {
            override fun onResult(qrResult: QRResult) {
                model.resultDialog(qrResult)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        model.onResume()
    }

    override fun onPause() {
        super.onPause()
        model.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager!!.releaseCamera()
        cameraManager!!.shutdownExecutor()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CODE_PICK_IMAGE) {
            val columns = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(data!!.data!!, columns, null, null, null)
            if (cursor!!.moveToFirst()) {
                Observable
                        .just(cursor.getString(cursor.getColumnIndex(columns[0])))
                        .observeOn(Schedulers.from(cameraManager!!.executor))
                        .compose(this.bindUntilEvent(ActivityEvent.PAUSE))
                        .map { str -> QRUtils.decode(str, MultiFormatReader()) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { qrResult -> model.resultDialog(qrResult!!) }
            }
            cursor.close()
        }
    }

    fun onSurfaceCreated(surfaceHolder: SurfaceHolder) {
        if (cameraManager!!.executor.isShutdown) return
        Observable
                .just(surfaceHolder)
                .compose(this.bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(Schedulers.from(cameraManager!!.executor))
                .map { holder ->
                    BaseCameraManager.setRotate(cameraManager!!, windowManager.defaultDisplay.rotation)
                    cameraManager!!.connectCamera(holder)
                    null
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    model.setEmptyViewVisible(false)
                    cameraManager!!.startCapture()
                }
    }

    fun onSurfaceDestroyed() {
        cameraManager!!.releaseCamera()
    }

    fun restartCapture() {
        cameraManager!!.startCapture()
    }

    fun setHook(hook: Boolean) {
        BaseCameraManager.setHook(cameraManager!!, hook)
    }

    companion object {
        const val CODE_PICK_IMAGE = 0x100
    }
}
