package com.pacific.barcode

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView

class QRCodeView : RelativeLayout {

    private var maskColor: Int = 0
    private var boxViewWidth: Int = 0
    private var boxViewHeight: Int = 0
    private var cornerColor: Int = 0
    private var borderColor: Int = 0
    private var cornerSize: Int = 0
    private var cornerLength: Int = 0
    private var cornerOffset: Int = 0

    private var boxView: FrameLayout? = null
    private var textView: TextView? = null
    private var lightOnClickListener: View.OnClickListener? = null

    constructor(context: Context) : super(context) {
        initialize(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context, attrs, defStyleAttr, 0)
    }

    @SuppressLint("NewApi")
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun initialize(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        View.inflate(context, R.layout.layout_qr_code_view, this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.QRCodeView, defStyleAttr, 0)
        val resources = resources
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            maskColor = typedArray.getColor(R.styleable.QRCodeView_maskColor, resources.getColor(R.color.qr_code_view_mask))
            cornerColor = typedArray.getColor(R.styleable.QRCodeView_boxViewCornerColor, resources.getColor(R.color.qr_code_view_corner))
            borderColor = typedArray.getColor(R.styleable.QRCodeView_boxViewBorderColor, resources.getColor(R.color.qr_code_view_border))
        } else {
            maskColor = typedArray.getColor(R.styleable.QRCodeView_boxViewCornerColor, resources.getColor(R.color.qr_code_view_mask, null))
            cornerColor = typedArray.getColor(R.styleable.QRCodeView_boxViewCornerColor, resources.getColor(R.color.qr_code_view_corner, null))
            borderColor = typedArray.getColor(R.styleable.QRCodeView_boxViewBorderColor, resources.getColor(R.color.qr_code_view_border, null))
        }

        cornerOffset = typedArray.getInt(R.styleable.QRCodeView_boxViewCornerOffset, resources.getDimension(R.dimen.size_qr_box_view_corner_offset).toInt())
        cornerLength = typedArray.getInt(R.styleable.QRCodeView_boxViewCornerLength, resources.getDimension(R.dimen.length_qr_box_view_corner).toInt())
        cornerSize = typedArray.getInt(R.styleable.QRCodeView_boxViewCornerSize, resources.getDimension(R.dimen.size_qr_box_view_corner).toInt())
        boxViewWidth = typedArray.getInt(R.styleable.QRCodeView_boxViewWidth, resources.getDimension(R.dimen.width_qr_box_view).toInt())
        boxViewHeight = typedArray.getInt(R.styleable.QRCodeView_boxViewHeight, resources.getDimension(R.dimen.height_qr_box_view).toInt())

        typedArray.recycle()
        boxView = findViewById<View>(R.id.fl_box_view) as FrameLayout
        textView = findViewById<View>(R.id.tv_desc) as TextView
        val params = boxView!!.layoutParams as RelativeLayout.LayoutParams
        params.width = boxViewWidth
        params.height = boxViewHeight
        boxView!!.layoutParams = params
        setBackgroundResource(R.color.qr_code_view_mask)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        findViewById<View>(R.id.btn_light).setOnClickListener { view ->
            val checkBox = view as CheckBox
            if (checkBox.isChecked) {
                checkBox.setText(R.string.action_light_off_desc)
            } else {
                checkBox.setText(R.string.action_light_on_desc)
            }
            if (lightOnClickListener != null) {
                lightOnClickListener!!.onClick(view)
            }
        }
        val animation = AnimationUtils.loadAnimation(context, R.anim.exlore_line_move)
        animation.interpolator = LinearInterpolator()
        findViewById<View>(R.id.img_scan_line).animation = animation
    }

    public override fun onDraw(canvas: Canvas) {
        /** Draw the exterior dark mask */
        val width = width
        val height = height
        val boxViewX = boxView!!.x
        val boxViewY = boxView!!.y

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = maskColor
        canvas.drawRect(0f, boxViewY, boxViewX, boxViewY + boxViewHeight, paint)// left rect
        canvas.drawRect(boxViewX + boxViewWidth, boxViewY, width.toFloat(), boxViewY + boxViewHeight, paint)// right rect
        canvas.drawRect(0f, 0f, width.toFloat(), boxViewY, paint)// top rect
        canvas.drawRect(0f, boxViewY + boxViewHeight, width.toFloat(), height.toFloat(), paint)// bottom rect

        /** Draw the border lines */
        paint.color = borderColor
        canvas.drawLine(boxViewX, boxViewY, boxViewX + boxViewWidth, boxViewY, paint)
        canvas.drawLine(boxViewX, boxViewY, boxViewX, boxViewY + boxViewHeight, paint)
        canvas.drawLine(boxViewX + boxViewWidth, boxViewY + boxViewHeight, boxViewX, boxViewY + boxViewHeight, paint)
        canvas.drawLine(boxViewX + boxViewWidth, boxViewY + boxViewHeight, boxViewX + boxViewWidth, boxViewY, paint)

        /** Draw the corners */
        val rect = Rect()
        rect.set(boxViewX.toInt(), boxViewY.toInt(), boxViewX.toInt() + boxViewWidth, boxViewY.toInt() + boxViewHeight)
        paint.color = cornerColor

        /** top the corners */
        canvas.drawRect((rect.left - cornerSize + cornerOffset).toFloat(), (rect.top - cornerSize + cornerOffset).toFloat(), (rect.left + cornerLength - cornerSize + cornerOffset).toFloat(), (rect.top + cornerOffset).toFloat(), paint)
        canvas.drawRect((rect.left - cornerSize + cornerOffset).toFloat(), (rect.top - cornerSize + cornerOffset).toFloat(), (rect.left + cornerOffset).toFloat(), (rect.top + cornerLength - cornerSize + cornerOffset).toFloat(), paint)
        canvas.drawRect((rect.right - cornerLength + cornerSize - cornerOffset).toFloat(), (rect.top - cornerSize + cornerOffset).toFloat(), (rect.right + cornerSize - cornerOffset).toFloat(), (rect.top + cornerOffset).toFloat(), paint)
        canvas.drawRect((rect.right - cornerOffset).toFloat(), (rect.top - cornerSize + cornerOffset).toFloat(), (rect.right + cornerSize - cornerOffset).toFloat(), (rect.top + cornerLength - cornerSize + cornerOffset).toFloat(), paint)

        /** bottom the corners */
        canvas.drawRect((rect.left - cornerSize + cornerOffset).toFloat(), (rect.bottom - cornerOffset).toFloat(), (rect.left + cornerLength - cornerSize + cornerOffset).toFloat(), (rect.bottom + cornerSize - cornerOffset).toFloat(), paint)
        canvas.drawRect((rect.left - cornerSize + cornerOffset).toFloat(), (rect.bottom - cornerLength + cornerSize - cornerOffset).toFloat(), (rect.left + cornerOffset).toFloat(), (rect.bottom + cornerSize - cornerOffset).toFloat(), paint)
        canvas.drawRect((rect.right - cornerLength + cornerSize - cornerOffset).toFloat(), (rect.bottom - cornerOffset).toFloat(), (rect.right + cornerSize - cornerOffset).toFloat(), (rect.bottom + cornerSize - cornerOffset).toFloat(), paint)
        canvas.drawRect((rect.right - cornerOffset).toFloat(), (rect.bottom - cornerLength + cornerSize - cornerOffset).toFloat(), (rect.right + cornerSize - cornerOffset).toFloat(), (rect.bottom + cornerSize - cornerOffset).toFloat(), paint)
    }

    fun setDescription(text: String) {
        if (textView != null) {
            textView!!.text = text
        }
    }

    fun setPickImageListener(onClickListener: View.OnClickListener?) {
        if (onClickListener != null) {
            findViewById<View>(R.id.btn_photo).setOnClickListener(onClickListener)
        }
    }

    fun setProduceQRListener(onClickListener: View.OnClickListener?) {
        if (onClickListener != null) {
            findViewById<View>(R.id.btn_produce).setOnClickListener(onClickListener)
        }
    }

    fun setLightOnClickListener(lightOnClickListener: View.OnClickListener) {
        this.lightOnClickListener = lightOnClickListener
    }
}