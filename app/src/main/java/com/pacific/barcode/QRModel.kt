package com.pacific.barcode

import com.pacific.mvc.ActivityModel

class QRModel(view: QRView) : ActivityModel<QRView>(view) {

    fun resultDialog(qrResult: QRResult) {
        view.resultDialog(qrResult)
    }

    fun onResume() {
        view.setSurfaceViewVisible(true)
    }

    fun onPause() {
        view.setSurfaceViewVisible(false)
    }

    fun setEmptyViewVisible(visible: Boolean) {
        view.setEmptyViewVisible(visible)
    }
}
