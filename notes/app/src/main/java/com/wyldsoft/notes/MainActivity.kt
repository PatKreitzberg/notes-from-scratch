package com.wyldsoft.notes

import android.app.Activity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.os.Build
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.onyx.android.sdk.api.device.epd.EpdController
import com.onyx.android.sdk.api.device.epd.UpdateMode
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.RawInputCallback
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.data.TouchPointList
import com.onyx.android.sdk.pen.style.StrokeStyle
import java.util.UUID
import android.graphics.Rect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Color
import android.graphics.RectF
import org.lsposed.hiddenapibypass.HiddenApiBypass


// Screen dimensions - will be initialized in onCreate
var SCREEN_WIDTH = 0
var SCREEN_HEIGHT = 0
var BACKGROUND_COLOR = Color.WHITE

/**
 * Ultra-simple drawing activity for Onyx e-ink devices
 * No Compose, no fancy structure - just the bare minimum to draw with stylus
 */
class SimpleDrawingActivity : Activity() {
    private lateinit var drawingSurface: SurfaceView
    private lateinit var touchHelper: TouchHelper
    private val TAG = "SimpleDrawingActivity"

    // Drawing state
    private val strokes = mutableListOf<SimpleStroke>()
    private var currentStroke: SimpleStroke? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d(TAG, "onCreate")
        checkHiddenApiBypass()

        // Initialize screen dimensions
        SCREEN_WIDTH = resources.displayMetrics.widthPixels
        SCREEN_HEIGHT = resources.displayMetrics.heightPixels

        // Enable full screen mode
        enableFullScreen()

        // Request necessary permissions
        requestPermissions()

        // Create SurfaceView directly
        drawingSurface = SurfaceView(this)
        drawingSurface.isFocusable = true
        drawingSurface.isFocusableInTouchMode = true

        // Set the content view
        setContentView(drawingSurface)

        // Set up the SurfaceHolder callback
        drawingSurface.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                initDrawing()
                //drawTestPattern()
                android.util.Log.d(TAG, "surfaceCreated")
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                initDrawing()
                //drawTestPattern()
                android.util.Log.d(TAG, "surfaceChanged: format=$format, width=$width, height=$height")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                android.util.Log.d(TAG, "surfaceDestroyed")
                touchHelper.closeRawDrawing()
            }
        })

        android.util.Log.d(TAG, "Activity created with dimensions: $SCREEN_WIDTH x $SCREEN_HEIGHT")
    }


    // Extension property to convert dp to pixels
    val Float.dp: Float
        get() = this * resources.displayMetrics.density

    val Int.dp: Float
        get() = this.toFloat() * resources.displayMetrics.density

//    private fun drawTestPattern() {
//        if (!drawingSurface.holder.surface.isValid) {
//            android.util.Log.w(TAG, "Surface not valid in drawTestPattern")
//            return
//        }
//
//        val canvas = drawingSurface.holder.lockCanvas() ?: run {
//            android.util.Log.w(TAG, "Could not lock canvas in drawTestPattern")
//            return
//        }
//
//        try {
//            // Clear the canvas with white background
//            canvas.drawColor(BACKGROUND_COLOR)
//
//            // Draw a border rectangle
//            val borderPaint = Paint().apply {
//                color = Color.BLACK
//                style = Paint.Style.STROKE
//                strokeWidth = 10.dp
//            }
//            canvas.drawRect(20.dp, 20.dp, SCREEN_WIDTH - 20.dp, SCREEN_HEIGHT - 20.dp, borderPaint)
//
//            // Draw some text
//            val textPaint = Paint().apply {
//                color = Color.BLACK
//                textSize = 48.dp
//                textAlign = Paint.Align.CENTER
//            }
//            canvas.drawText("Drawing Test", SCREEN_WIDTH / 2f, 100.dp, textPaint)
//
//            // Draw a circle in the middle
//            val circlePaint = Paint().apply {
//                color = Color.BLACK
//                style = Paint.Style.FILL
//            }
//            canvas.drawCircle(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 100.dp, circlePaint)
//
//            // Draw some diagonal lines
//            val linePaint = Paint().apply {
//                color = Color.BLACK
//                strokeWidth = 5.dp
//                style = Paint.Style.STROKE
//            }
//            canvas.drawLine(20.dp, 20.dp, SCREEN_WIDTH - 20.dp, SCREEN_HEIGHT - 20.dp, linePaint)
//            canvas.drawLine(SCREEN_WIDTH - 20.dp, 20.dp, 20.dp, SCREEN_HEIGHT - 20.dp, linePaint)
//
//            android.util.Log.d(TAG, "Drew test pattern")
//        } catch (e: Exception) {
//            android.util.Log.e(TAG, "Error drawing test pattern: ${e.message}", e)
//        } finally {
//            drawingSurface.holder.unlockCanvasAndPost(canvas)
//        }
//
//        // Refresh the screen to ensure the e-ink display updates
//        refreshScreen()
//    }

    private fun checkHiddenApiBypass() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d(TAG, "onResume")
        if (::touchHelper.isInitialized && drawingSurface.holder.surface.isValid) {
            initDrawing()
        }
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.d(TAG, "onPause")
        if (::touchHelper.isInitialized) {
            touchHelper.closeRawDrawing()
        }
    }

    private fun initDrawing() {
        android.util.Log.d(TAG, "initDrawing")

        if (!drawingSurface.holder.surface.isValid) {
            android.util.Log.e(TAG, "Surface not valid in initDrawing")
            return
        }

        android.util.Log.d(TAG, "before try")
        try {
            // Close existing TouchHelper if it exists
            android.util.Log.d(TAG, "try 1")
            if (::touchHelper.isInitialized) {
                touchHelper.closeRawDrawing()
            }
            android.util.Log.d(TAG, "try 2")
            // Create TouchHelper with callback
            touchHelper = TouchHelper.create(drawingSurface, object : RawInputCallback() {
                override fun onBeginRawDrawing(b: Boolean, touchPoint: TouchPoint?) {
                    android.util.Log.d(TAG, "onBeginRawDrawing: $b, point: ${touchPoint?.x}, ${touchPoint?.y}")

                    // Start a new stroke
                    val strokeId = UUID.randomUUID().toString()
                    currentStroke = SimpleStroke(strokeId)
                    strokes.add(currentStroke!!)

                    // Add the first point if available
                    touchPoint?.let {
                        currentStroke?.points?.add(SimplePoint(it.x, it.y, it.pressure))
                    }
                }

                override fun onEndRawDrawing(b: Boolean, touchPoint: TouchPoint?) {
                    android.util.Log.d(TAG, "onEndRawDrawing: $b, points in stroke: ${currentStroke?.points?.size ?: 0}")

                    // Add the last point if available
                    touchPoint?.let {
                        currentStroke?.points?.add(SimplePoint(it.x, it.y, it.pressure))
                    }

                    // Complete the stroke
                    currentStroke = null

                    // Force a refresh to show the complete stroke
                    drawAllStrokes()
                    refreshScreen()
                }

                override fun onRawDrawingTouchPointMoveReceived(point: TouchPoint?) {
                    // Optional detailed logging of individual points
                    point?.let {
                        android.util.Log.v(TAG, "Point move: ${it.x}, ${it.y}, pressure: ${it.pressure}")
                    }
                }

                override fun onRawDrawingTouchPointListReceived(pointList: TouchPointList) {
                    android.util.Log.d(TAG, "Received ${pointList.size()} points")

                    // Add points to the current stroke
                    currentStroke?.let { stroke ->
                        pointList.points.forEach { point ->
                            stroke.points.add(SimplePoint(point.x, point.y, point.pressure))
                        }

                        // Draw the new part of the stroke
                        drawCurrentStroke()
                    }
                }

                // We don't need eraser functionality for minimal implementation
                override fun onBeginRawErasing(p0: Boolean, p1: TouchPoint?) {}
                override fun onEndRawErasing(p0: Boolean, p1: TouchPoint?) {}
                override fun onRawErasingTouchPointMoveReceived(p0: TouchPoint?) {}
                override fun onRawErasingTouchPointListReceived(p0: TouchPointList?) {}

                override fun onPenUpRefresh(refreshRect: RectF?) {
                    super.onPenUpRefresh(refreshRect)
                    android.util.Log.d(TAG, "onPenUpRefresh")
                }

                override fun onPenActive(point: TouchPoint?) {
                    super.onPenActive(point)
                    android.util.Log.d(TAG, "onPenActive")
                }
            })

            android.util.Log.d(TAG, "touchhelper $touchHelper")

            // CRITICAL: Set limit rect and configure TouchHelper
            val limitRect = mutableListOf<Rect>()
            limitRect.add(Rect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT))
            touchHelper.setLimitRect(mutableListOf(Rect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT)))
            touchHelper.setStrokeStyle(StrokeStyle.PENCIL)
            touchHelper.setStrokeWidth(5.0f)
            touchHelper.setStrokeColor(Color.BLACK)
            touchHelper.openRawDrawing()
            touchHelper.setRawDrawingEnabled(true)

            touchHelper.setExcludeRect(mutableListOf<Rect>()) // user can't draw in the rects in the setExcludeRect list

            // Draw any existing strokes
            drawAllStrokes()
            //setupStandardTouchListener()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error initializing TouchHelper: ${e.message}", e)
            println("booger error init touchhelper")
        }
    }

    private fun drawCurrentStroke() {
        if (!drawingSurface.holder.surface.isValid) {
            android.util.Log.w(TAG, "Surface not valid in drawCurrentStroke")
            return
        }

        val canvas = drawingSurface.holder.lockCanvas() ?: run {
            android.util.Log.w(TAG, "Could not lock canvas in drawCurrentStroke")
            return
        }

        try {
            currentStroke?.let { stroke ->
                if (stroke.points.size < 2) {
                    android.util.Log.d(TAG, "Not enough points to draw")
                    return@let
                }

                val paint = Paint().apply {
                    color = Color.BLACK
                    strokeWidth = 5.0f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    isAntiAlias = true
                }

                val path = Path()

                // Start from the beginning for consistent drawing
                path.moveTo(stroke.points[0].x, stroke.points[0].y)

                // Draw all points in the stroke
                for (i in 1 until stroke.points.size) {
                    path.lineTo(stroke.points[i].x, stroke.points[i].y)
                }

                canvas.drawPath(path, paint)
                android.util.Log.d(TAG, "Drew current stroke with ${stroke.points.size} points")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error drawing current stroke: ${e.message}", e)
        } finally {
            drawingSurface.holder.unlockCanvasAndPost(canvas)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupStandardTouchListener() {
        drawingSurface.setOnTouchListener { _, event ->
            val x = event.x
            val y = event.y
            val pressure = event.pressure

            android.util.Log.d(TAG, "touch listener $x  $y is touchHelper.isRawDrawingInputEnabled() ${touchHelper.isRawDrawingInputEnabled()}")

            // Return false to allow the event to be processed by other listeners
            return@setOnTouchListener false
        }
    }

    private fun drawAllStrokes() {
        if (!drawingSurface.holder.surface.isValid) {
            android.util.Log.w(TAG, "Surface not valid in drawAllStrokes")
            return
        }

        val canvas = drawingSurface.holder.lockCanvas() ?: run {
            android.util.Log.w(TAG, "Could not lock canvas in drawAllStrokes")
            return
        }

        try {
            // Clear the canvas first
            canvas.drawColor(BACKGROUND_COLOR)

            val paint = Paint().apply {
                color = Color.BLACK
                strokeWidth = 5.0f  // Thicker stroke for better visibility on e-ink
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }

            var strokesDrawn = 0
            strokes.forEach { stroke ->
                if (stroke.points.size < 2) return@forEach

                val path = Path()
                path.moveTo(stroke.points[0].x, stroke.points[0].y)

                for (i in 1 until stroke.points.size) {
                    path.lineTo(stroke.points[i].x, stroke.points[i].y)
                }

                canvas.drawPath(path, paint)
                strokesDrawn++
            }

            android.util.Log.d(TAG, "Drew $strokesDrawn strokes")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error drawing all strokes: ${e.message}", e)
        } finally {
            drawingSurface.holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun refreshScreen() {
        // Use Onyx EpdController for screen refresh for e-ink displays
        if (drawingSurface.holder.surface.isValid) {
            try {
                android.util.Log.d(TAG, "Refreshing screen")
                EpdController.refreshScreen(drawingSurface, UpdateMode.GC)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error refreshing screen: ${e.message}", e)
            }
        }
    }

    private fun enableFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.decorView.post {
                window.insetsController?.let {
                    it.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        } else {
            // For devices running Android 10 (Q) or below
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            android.util.Log.d(TAG, "request permissions if")
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1001
                )
            }
        } else if (!Environment.isExternalStorageManager()) {
            android.util.Log.d(TAG, "request permissions else")
            val intent = android.content.Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = android.net.Uri.fromParts("package", packageName, null)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
}

// Simple data classes for stroke handling
data class SimplePoint(val x: Float, val y: Float, val pressure: Float)

class SimpleStroke(val id: String) {
    val points = mutableListOf<SimplePoint>()
}