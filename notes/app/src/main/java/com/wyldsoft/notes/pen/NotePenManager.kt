package com.wyldsoft.notes.pen

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.view.SurfaceView
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.data.TouchPointList
import java.util.*

class NotePenManager(private val context: Context) {
    companion object {
        const val PEN_TYPE_FOUNTAIN = 1
        const val PEN_TYPE_BRUSH = 3
        const val PEN_TYPE_PENCIL = 0
        const val PEN_TYPE_CHARCOAL = 4
        const val PEN_TYPE_MARKER = 2

        // Default values
        const val DEFAULT_STROKE_WIDTH = 3.0f
        const val DEFAULT_COLOR = Color.BLUE
    }

    private var touchHelper: TouchHelper? = null
    private var currentPenType: Int = PEN_TYPE_FOUNTAIN
    private var currentStrokeWidth: Float = DEFAULT_STROKE_WIDTH
    private var currentColor: Int = DEFAULT_COLOR
    private var isErasing: Boolean = false

    // Callback for pen input
    private val rawInputCallback = object : RawInputCallback() {
        override fun onBeginRawDrawing(b: Boolean, touchPoint: TouchPoint?) {
            // Handle start of drawing
            println("onBeginRawDraing")
        }

        override fun onEndRawDrawing(b: Boolean, touchPoint: TouchPoint?) {
            // Handle end of drawing
            println("onEndRawDraing")
        }

        override fun onRawDrawingTouchPointMoveReceived(touchPoint: TouchPoint?) {
            // Handle drawing movement
        }

        override fun onRawDrawingTouchPointListReceived(touchPointList: TouchPointList?) {
            // Handle drawing points
        }

        override fun onBeginRawErasing(b: Boolean, touchPoint: TouchPoint?) {
            // Handle start of erasing
        }

        override fun onEndRawErasing(b: Boolean, touchPoint: TouchPoint?) {
            // Handle end of erasing
        }

        override fun onRawErasingTouchPointMoveReceived(touchPoint: TouchPoint?) {
            // Handle erasing movement
        }

        override fun onRawErasingTouchPointListReceived(touchPointList: TouchPointList?) {
            // Handle erasing points
        }

        override fun onPenUpRefresh(refreshRect: RectF?) {
            // Handle pen up refresh
        }
    }

    fun initialize(surfaceView: SurfaceView, limitRect: Rect, excludeRects: List<Rect>) {
        touchHelper = TouchHelper.create(surfaceView, rawInputCallback)
        touchHelper?.apply {
            setLimitRect(limitRect, excludeRects)
            setStrokeWidth(currentStrokeWidth)
            setStrokeColor(currentColor)
            openRawDrawing()
        }
        touchHelper?.setRawDrawingEnabled(true)
    }

    fun clearCanvas(surfaceView: SurfaceView) {
        val holder = surfaceView.holder
        if (holder == null || !holder.surface.isValid) {
            return
        }

        try {
            val canvas = surfaceView.holder.lockCanvas()//?: run {                return            }
            println("clearCanvas 4")
            if (canvas != null) {
                // Fill the canvas with white
                canvas.drawColor(Color.WHITE)
                holder.unlockCanvasAndPost(canvas)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentPenType(): Int {
        return currentPenType
    }

    fun getCurrentColor(): Int {
        return currentColor
    }

    fun setCurrentPenType(penType: Int) {
        currentPenType = penType
        updatePenStyle()
    }

    fun setStrokeWidth(width: Float) {
        currentStrokeWidth = width
        touchHelper?.setStrokeWidth(width)
    }

    fun setStrokeColor(color: Int) {
        currentColor = color
        touchHelper?.setStrokeColor(color)
    }

    fun setErasing(erase: Boolean) {
        isErasing = erase
        if (erase) {
            // Switch to eraser mode
            touchHelper?.setRawDrawingEnabled(false)
            touchHelper?.setRawDrawingEnabled(true)
        } else {
            // Switch back to pen mode
            updatePenStyle()
        }
    }

    private fun updatePenStyle() {
        val strokeStyle = when (currentPenType) {
            PEN_TYPE_FOUNTAIN -> TouchHelper.STROKE_STYLE_FOUNTAIN
            PEN_TYPE_BRUSH -> TouchHelper.STROKE_STYLE_NEO_BRUSH
            PEN_TYPE_PENCIL -> TouchHelper.STROKE_STYLE_PENCIL
            PEN_TYPE_CHARCOAL -> TouchHelper.STROKE_STYLE_CHARCOAL
            PEN_TYPE_MARKER -> TouchHelper.STROKE_STYLE_MARKER
            else -> TouchHelper.STROKE_STYLE_PENCIL
        }

        touchHelper?.apply {
            setStrokeStyle(strokeStyle)
            setStrokeWidth(currentStrokeWidth)
            setStrokeColor(currentColor)
        }
    }

    fun destroy() {
        touchHelper?.closeRawDrawing()
        touchHelper = null
    }
}