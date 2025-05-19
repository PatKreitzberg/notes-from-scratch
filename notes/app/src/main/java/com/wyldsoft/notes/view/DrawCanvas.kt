package com.wyldsoft.notes.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.wyldsoft.notes.pen.PenManager
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.api.device.epd.UpdateMode
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.data.TouchPointList

class DrawCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private var penManager: PenManager? = null
    private var initialized = false

    init {
        holder.addCallback(this)
    }

    fun initialize(penManager: PenManager) {
        this.penManager = penManager
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        clearSurface()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (initialized) return

        clearSurface()

        // Connect to pen manager for drawing
        penManager?.attachSurfaceView(this, createInputCallback())

        initialized = true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        initialized = false
    }

    fun release() {
        penManager?.closeRawDrawing()
    }

    private fun clearSurface() {
        val canvas = holder.lockCanvas() ?: return
        canvas.drawColor(Color.WHITE)
        holder.unlockCanvasAndPost(canvas)
    }

    private fun createInputCallback(): RawInputCallback {
        return object : RawInputCallback() {
            override fun onBeginRawDrawing(b: Boolean, touchPoint: TouchPoint) {
                // Start drawing
            }

            override fun onEndRawDrawing(b: Boolean, touchPoint: TouchPoint) {
                // End drawing
            }

            override fun onRawDrawingTouchPointMoveReceived(touchPoint: TouchPoint) {
                // Touch point movement
            }

            override fun onRawDrawingTouchPointListReceived(touchPointList: TouchPointList) {
                // Received touch points list
            }

            override fun onBeginRawErasing(b: Boolean, touchPoint: TouchPoint) {
                // Start erasing
            }

            override fun onEndRawErasing(b: Boolean, touchPoint: TouchPoint) {
                // End erasing
            }

            override fun onRawErasingTouchPointMoveReceived(touchPoint: TouchPoint) {
                // Eraser moving
            }

            override fun onRawErasingTouchPointListReceived(touchPointList: TouchPointList) {
                // Eraser touch points received
            }

            override fun onPenUpRefresh(rectF: android.graphics.RectF) {
                // Refresh the e-ink display in the specified area
                EpdController.refreshRect(this@DrawCanvas, rectF, UpdateMode.HAND_WRITING_REPAINT_MODE)
            }
        }
    }
}