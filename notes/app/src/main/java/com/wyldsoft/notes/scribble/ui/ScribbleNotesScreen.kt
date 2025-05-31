package com.wyldsoft.notes.scribble.ui

import android.view.SurfaceView
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ScribbleNotesScreen(
    viewModel: ScribbleViewModel,
    onSurfaceViewCreated: (SurfaceView) -> Unit
) {
    val showProfileEditPopup by viewModel.showProfileEditPopup
    val editingProfileIndex by viewModel.editingProfileIndex

    println("DEBUG: ScribbleNotesScreen recomposing - showProfileEditPopup: $showProfileEditPopup, editingProfileIndex: $editingProfileIndex")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Toolbar
        PenToolbar(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.White)
                .shadow(4.dp)
                .padding(8.dp)
        )

        // Drawing Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AndroidView(
                factory = { context ->
                    SurfaceView(context).apply {
                        // Configure the surface view
                        holder.setFixedSize(0, 0) // Will be set by layout
                        // Set a unique ID so it can be properly referenced
                        id = View.generateViewId()
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { surfaceView ->
                    // Call the callback whenever the view is updated
                    onSurfaceViewCreated(surfaceView)
                }
            )
        }
    }

    // Profile Edit Popup
    println("DEBUG: Checking dialog conditions - showProfileEditPopup: $showProfileEditPopup, editingProfileIndex: $editingProfileIndex")

    if (showProfileEditPopup && editingProfileIndex >= 0) {
        println("DEBUG: Rendering ProfileEditDialog for profile $editingProfileIndex")
        ProfileEditDialog(
            profile = viewModel.penProfiles.value[editingProfileIndex],
            onProfileChanged = { updatedProfile ->
                println("DEBUG: Profile changed in dialog")
                viewModel.updateProfile(editingProfileIndex, updatedProfile)
            },
            onDismiss = {
                println("DEBUG: Dialog dismissed")
                viewModel.hideProfileEditPopup()
            }
        )
    } else {
        println("DEBUG: Not showing dialog - conditions not met")
    }
}

@Composable
fun PenToolbar(
    viewModel: ScribbleViewModel,
    modifier: Modifier = Modifier
) {
    val penProfiles by viewModel.penProfiles
    val selectedProfileIndex by viewModel.selectedProfileIndex

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        penProfiles.forEachIndexed { index, profile ->
            PenToolbarButton(
                profile = profile,
                isSelected = index == selectedProfileIndex,
                onClick = { viewModel.selectPenProfile(index) },
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun PenToolbarButton(
    profile: PenProfile,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = androidx.compose.ui.graphics.Color(profile.strokeColor)
    val borderColor = if (isSelected) Color.Black else Color.Gray
    val borderWidth = if (isSelected) 4.dp else 1.dp

    Button(
        onClick = {
            println("DEBUG: PenToolbarButton clicked, isSelected: $isSelected") // Debug log
            onClick()
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
        contentPadding = PaddingValues(8.dp)
    ) {
        Icon(
            painter = getIconForStrokeStyle(profile.strokeStyle),
            contentDescription = getContentDescriptionForStyle(profile.strokeStyle),
            tint = getContrastingColor(backgroundColor),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun getIconForStrokeStyle(strokeStyle: Int): androidx.compose.ui.graphics.painter.Painter {
    // You'll need to convert your drawable resources to vector drawables or use painterResource
    // This is a simplified version - you should use your actual icon resources
    return when (strokeStyle) {
        com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_CHARCOAL ->
            androidx.compose.ui.res.painterResource(com.wyldsoft.notes.R.drawable.ic_marker_pen)
        com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_FOUNTAIN ->
            androidx.compose.ui.res.painterResource(com.wyldsoft.notes.R.drawable.ic_pen_fountain)
        com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_MARKER ->
            androidx.compose.ui.res.painterResource(com.wyldsoft.notes.R.drawable.ic_pen_hard)
        com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_NEO_BRUSH ->
            androidx.compose.ui.res.painterResource(com.wyldsoft.notes.R.drawable.ic_pen_soft)
        com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_PENCIL ->
            androidx.compose.ui.res.painterResource(com.wyldsoft.notes.R.drawable.ic_charcoal_pen)
        else -> androidx.compose.ui.res.painterResource(com.wyldsoft.notes.R.drawable.ic_pen_fountain)
    }
}

private fun getContentDescriptionForStyle(strokeStyle: Int): String {
    return when (strokeStyle) {
        com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_CHARCOAL -> "Charcoal Pen"
        com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_FOUNTAIN -> "Fountain Pen"
        com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_MARKER -> "Marker"
        com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_NEO_BRUSH -> "Neo Brush"
        com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_PENCIL -> "Pencil"
        else -> "Pen"
    }
}

private fun getContrastingColor(backgroundColor: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color {
    // Simple contrast calculation - you might want to use a more sophisticated method
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return if (luminance > 0.5) Color.Black else Color.White
}