package com.wyldsoft.notes.scribble.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

import com.wyldsoft.notes.scribble.broadcast.GlobalDeviceReceiver
import com.wyldsoft.notes.scribble.request.RendererToScreenRequest
import com.wyldsoft.notes.scribble.util.TouchUtils
import com.wyldsoft.notes.ui.theme.NotesTheme

import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.data.TouchPointList
import com.onyx.android.sdk.rx.RxManager
import java.util.ArrayList

class ScribbleNotes : ComponentActivity() {
    private val TAG = ScribbleNotes::class.java.simpleName

    private val deviceReceiver = GlobalDeviceReceiver()
    private var rxManager: RxManager? = null
    private var touchHelper: TouchHelper? = null
    private val paint = Paint()
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var surfaceView: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        deviceReceiver.enable(this, true)

        setContent {
            NotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ScribbleViewModel = viewModel()

                    ScribbleNotesScreen(
                        viewModel = viewModel,
                        onSurfaceViewCreated = { surfaceView ->
                            this@ScribbleNotes.surfaceView = surfaceView
                            initializeTouchHelper(surfaceView, viewModel)
                        }
                    )
                }
            }
        }

        initReceiver()
    }

    private fun initializeTouchHelper(surfaceView: SurfaceView, viewModel: ScribbleViewModel) {
        touchHelper = TouchHelper.create(surfaceView, createRawInputCallback())

        surfaceView.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (cleanSurfaceView(surfaceView)) {
                // Layout change handled
            }

            val limit = android.graphics.Rect()
            surfaceView.getLocalVisibleRect(limit)

            touchHelper?.apply {
                val currentProfile = viewModel.currentPenProfile.value
                println("DEBUG: Initializing TouchHelper with profile - Color: ${currentProfile.strokeColor}, Style: ${currentProfile.strokeStyle}, Size: ${currentProfile.strokeSize}")

                setStrokeWidth(currentProfile.strokeSize)
                    .setLimitRect(limit, ArrayList())
                    .openRawDrawing()

                setStrokeStyle(currentProfile.strokeStyle)
                setStrokeColor(currentProfile.strokeColor)

                // Set drawing enabled based on ViewModel state
                val isEnabled = viewModel.isDrawingEnabled.value
                setRawDrawingEnabled(isEnabled)
                setRawDrawingRenderEnabled(isEnabled)

                println("DEBUG: TouchHelper initialized - drawing enabled: $isEnabled")
            }
        }

        val surfaceCallback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                cleanSurfaceView(surfaceView)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                holder.removeCallback(this)
            }
        }
        surfaceView.holder.addCallback(surfaceCallback)

        // Observe pen profile changes
        viewModel.currentPenProfileLiveData.observe(this) { profile ->
            println("DEBUG: Profile change observed in Activity")
            updatePenSettings(profile)
        }

        // Observe drawing enabled state changes
        viewModel.isDrawingEnabledLiveData.observe(this) { isEnabled ->
            println("DEBUG: Drawing enabled state changed: $isEnabled")
            touchHelper?.setRawDrawingEnabled(isEnabled)
        }

        // Initialize paint with current profile
        val initialProfile = viewModel.currentPenProfile.value
        updatePenSettings(initialProfile)
    }

    private fun updatePenSettings(profile: PenProfile) {
        println("DEBUG: updatePenSettings called - Color: ${profile.strokeColor}, Style: ${profile.strokeStyle}, Size: ${profile.strokeSize}")

        touchHelper?.apply {
            setStrokeColor(profile.strokeColor)
            setStrokeStyle(profile.strokeStyle)
            setStrokeWidth(profile.strokeSize)
            println("DEBUG: TouchHelper settings updated")
        }

        paint.color = profile.strokeColor
        paint.strokeWidth = profile.strokeSize
        println("DEBUG: Paint settings updated")
    }

    override fun onResume() {
        touchHelper?.let { th ->
            println("DEBUG: onResume - re-enabling drawing")
            th.setRawDrawingEnabled(true)
        }
        super.onResume()
    }

    override fun onDestroy() {
        touchHelper?.closeRawDrawing()
        bitmap?.recycle()
        bitmap = null
        deviceReceiver.enable(this, false)
        super.onDestroy()
    }

    private fun initReceiver() {
        deviceReceiver.setSystemNotificationPanelChangeListener { open ->
            touchHelper?.setRawDrawingEnabled(!open)
            bitmap?.let { surfaceView?.let { sv -> renderToScreen(sv, it) } }
        }.setSystemScreenOnListener {
            bitmap?.let { surfaceView?.let { sv -> renderToScreen(sv, it) } }
        }
    }

    private fun cleanSurfaceView(surfaceView: SurfaceView): Boolean {
        if (surfaceView.holder == null) return false

        val canvas = surfaceView.holder.lockCanvas() ?: return false

        canvas.drawColor(Color.WHITE)
        surfaceView.holder.unlockCanvasAndPost(canvas)
        return true
    }

    private fun createRawInputCallback() = object : RawInputCallback() {
        override fun onBeginRawDrawing(b: Boolean, touchPoint: TouchPoint) {
            TouchUtils.disableFingerTouch(applicationContext)
        }

        override fun onEndRawDrawing(b: Boolean, touchPoint: TouchPoint) {
            TouchUtils.enableFingerTouch(applicationContext)
        }

        override fun onRawDrawingTouchPointMoveReceived(touchPoint: TouchPoint) {}

        override fun onRawDrawingTouchPointListReceived(touchPointList: TouchPointList) {
            drawScribbleToBitmap(touchPointList.points)
        }

        override fun onBeginRawErasing(b: Boolean, touchPoint: TouchPoint) {}
        override fun onEndRawErasing(b: Boolean, touchPoint: TouchPoint) {}
        override fun onRawErasingTouchPointMoveReceived(touchPoint: TouchPoint) {}
        override fun onRawErasingTouchPointListReceived(touchPointList: TouchPointList) {}
    }

    private fun drawScribbleToBitmap(list: List<TouchPoint>) {
        val currentSurfaceView = surfaceView ?: return

        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(
                currentSurfaceView.width,
                currentSurfaceView.height,
                Bitmap.Config.ARGB_8888
            )
            canvas = Canvas(bitmap!!)
        }

        canvas?.let { canvas ->
            val bitmapPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                color = paint.color
                strokeWidth = paint.strokeWidth
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }

            val path = Path()
            val prePoint = PointF(list[0].x, list[0].y)
            path.moveTo(prePoint.x, prePoint.y)

            for (point in list) {
                path.quadTo(prePoint.x, prePoint.y, point.x, point.y)
                prePoint.x = point.x
                prePoint.y = point.y
            }

            canvas.drawPath(path, bitmapPaint)
        }
    }

    private fun getRxManager(): RxManager {
        if (rxManager == null) {
            rxManager = RxManager.Builder.sharedSingleThreadManager()
        }
        return rxManager!!
    }

    private fun renderToScreen(surfaceView: SurfaceView, bitmap: Bitmap) {
        getRxManager().enqueue(RendererToScreenRequest(surfaceView, bitmap), null)
    }
}