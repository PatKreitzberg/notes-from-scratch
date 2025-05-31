package com.wyldsoft.notes.scribble.ui

import android.view.SurfaceView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ScribbleSurface(
    viewModel: ScribbleViewModel,
    onSurfaceViewCreated: (SurfaceView) -> Unit
) {
    val isDrawingEnabled by viewModel.isDrawingEnabled

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                SurfaceView(context).apply {
                    id = android.view.View.generateViewId()
                    // Configure surface view
                    holder.setFixedSize(0, 0)

                    // Notify parent about surface creation
                    onSurfaceViewCreated(this)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { surfaceView ->
                // Update touch handling based on drawing state
                surfaceView.isClickable = isDrawingEnabled
                surfaceView.isFocusable = isDrawingEnabled

                println("DEBUG: ScribbleSurface updated - drawing enabled: $isDrawingEnabled")
            }
        )
    }
}