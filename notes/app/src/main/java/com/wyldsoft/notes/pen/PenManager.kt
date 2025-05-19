package com.wyldsoft.notes.pen

import android.graphics.Color
import android.graphics.Rect
import android.view.SurfaceView
import android.content.Context
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.data.TouchPointList

class PenManager(private val context: Context) {
    private var touchHelper: TouchHelper? = null
    private var surfaceView: SurfaceView? = null
    private var callback: RawInputCallback? = null

    // Default values
    private var currentProfile = StrokeProfile.PENCIL
    private var currentStrokeWidth = 3.0f
    private var currentStrokeColor = Color.BLACK
    private var isErasing = false
    private var eraserWidth = 20.0f

    fun attachSurfaceView(surfaceView: SurfaceView, callback: RawInputCallback) {
        this.surfaceView = surfaceView
        this.callback = callback

        if (touchHelper == null) {
            touchHelper = TouchHelper.create(surfaceView, callback)
        } else {
            touchHelper?.bindHostView(surfaceView, callback)
        }

        updateStrokeStyle()
        openRawDrawing()
    }

    fun openRawDrawing() {
        val limitRect = Rect()
        surfaceView?.getLocalVisibleRect(limitRect)

        touchHelper?.setLimitRect(limitRect, ArrayList<Rect>())
            ?.setStrokeWidth(currentStrokeWidth)
            ?.openRawDrawing()
    }

    fun setStrokeProfile(profile: StrokeProfile) {
        currentProfile = profile
        isErasing = false
        updateStrokeStyle()
    }

    fun setStrokeWidth(width: Float) {
        currentStrokeWidth = width
        touchHelper?.setStrokeWidth(width)
    }

    fun setStrokeColor(color: Int) {
        currentStrokeColor = color
        touchHelper?.setStrokeColor(color)
    }

    fun setErasing(erasing: Boolean) {
        isErasing = erasing
        if (erasing) {
            touchHelper?.setRawDrawingEnabled(false)
            touchHelper?.setStrokeWidth(eraserWidth)
            touchHelper?.setRawDrawingEnabled(true)
        } else {
            updateStrokeStyle()
        }
    }

    fun setEraserWidth(width: Float) {
        eraserWidth = width
        if (isErasing) {
            touchHelper?.setStrokeWidth(width)
        }
    }

    private fun updateStrokeStyle() {
        touchHelper?.setRawDrawingEnabled(false)

        // Set stroke style based on profile
        when (currentProfile) {
            StrokeProfile.PENCIL -> touchHelper?.setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL)
            StrokeProfile.BRUSH -> touchHelper?.setStrokeStyle(TouchHelper.STROKE_STYLE_FOUNTAIN)
            StrokeProfile.MARKER -> touchHelper?.setStrokeStyle(TouchHelper.STROKE_STYLE_MARKER)
            StrokeProfile.CHARCOAL -> touchHelper?.setStrokeStyle(TouchHelper.STROKE_STYLE_CHARCOAL)
        }

        // Set color and width
        touchHelper?.setStrokeColor(currentStrokeColor)
        touchHelper?.setStrokeWidth(currentStrokeWidth)

        touchHelper?.setRawDrawingEnabled(true)
    }

    fun closeRawDrawing() {
        touchHelper?.closeRawDrawing()
        touchHelper = null
    }

    fun getCurrentStrokeWidth(): Float = currentStrokeWidth

    fun getCurrentStrokeColor(): Int = currentStrokeColor

    fun getCurrentProfile(): StrokeProfile = currentProfile

    fun isErasing(): Boolean = isErasing

    fun getEraserWidth(): Float = eraserWidth
}