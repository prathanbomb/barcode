package com.pacific.barcode

import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.pacific.mvc.ActivityView

/**
 * Created by root on 16-5-8.
 */
class QRView(activity: QRActivity) : ActivityView<QRActivity>(activity), SurfaceHolder.Callback {
    private var qrCodeView: QRCodeView? = null
    private var surfaceView: SurfaceView? = null

    override fun findView() {
        surfaceView = retrieveView(R.id.sv_preview)
        qrCodeView = retrieveView(R.id.qr_view)
    }

    override fun setListener() {
        qrCodeView!!.setPickImageListener(View.OnClickListener {
            activity.setHook(true)
            val galleryIntent = Intent()
            if (Build.VERSION_CODES.KITKAT >= Build.VERSION.SDK_INT) {
                galleryIntent.action = Intent.ACTION_OPEN_DOCUMENT
            } else {
                galleryIntent.action = Intent.ACTION_GET_CONTENT
            }
            galleryIntent.type = "image/*"
            val wrapperIntent = Intent.createChooser(galleryIntent, "选择二维码图片")
            activity.startIntentForResult(wrapperIntent, QRActivity.CODE_PICK_IMAGE, null)
        })
        surfaceView!!.holder.addCallback(this)
    }

    override fun setAdapter() {

    }

    override fun initialize() {

    }

    override fun onClick(v: View) {

    }

    fun resultDialog(qrResult: QRResult?) {
        if (qrResult == null) {
            AlertDialog.Builder(activity)
                    .setTitle("No Barcode Result")
                    .setMessage("Can't decode barcode from target picture , \nplease confirm the picture has barcode value.")
                    .setPositiveButton("Ok", null)
                    .setOnDismissListener {
                        activity.setHook(false)
                        activity.restartCapture()
                    }
                    .create()
                    .show()
            return
        }
        val view = activity.layoutInflater.inflate(R.layout.dialog_result, null)
        if (!TextUtils.isEmpty(qrResult.result.toString())) {
            (view.findViewById<View>(R.id.tv_value) as TextView).text = qrResult.result.toString()
        }
        if (qrResult.bitmap != null) {
            (view.findViewById<View>(R.id.img_barcode) as ImageView).setImageBitmap(qrResult.bitmap)
        }
        AlertDialog.Builder(activity)
                .setOnDismissListener {
                    activity.setHook(false)
                    activity.restartCapture()
                }
                .setView(view)
                .create()
                .show()
    }

    fun setEmptyViewVisible(visible: Boolean) {
        if (visible) {
            retrieveView<View>(R.id.v_empty).visibility = View.VISIBLE
        } else {
            retrieveView<View>(R.id.v_empty).visibility = View.GONE
        }
    }

    fun setSurfaceViewVisible(visible: Boolean) {
        if (visible) {
            surfaceView!!.visibility = View.VISIBLE
        } else {
            surfaceView!!.visibility = View.INVISIBLE
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        activity.onSurfaceCreated(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        setEmptyViewVisible(true)
        activity.onSurfaceDestroyed()
    }
}
