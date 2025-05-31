package com.wyldsoft.notes.scribble.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun ProfileEditDialog(
    profile: PenProfile,
    onProfileChanged: (PenProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var workingProfile by remember { mutableStateOf(profile.copy()) }

    // Render directly in the tree like StrokeOptionPanel in reference app
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Pen Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }

            // Stroke Style Section
            StrokeStyleSection(
                selectedStyle = workingProfile.strokeStyle,
                onStyleSelected = { newStyle ->
                    workingProfile = workingProfile.copy(strokeStyle = newStyle)
                    onProfileChanged(workingProfile)
                }
            )

            // Color Section
            ColorSection(
                selectedColor = workingProfile.strokeColor,
                onColorSelected = { newColor ->
                    workingProfile = workingProfile.copy(strokeColor = newColor)
                    onProfileChanged(workingProfile)
                }
            )

            // Size Section
            SizeSection(
                currentSize = workingProfile.strokeSize,
                onSizeChanged = { newSize ->
                    workingProfile = workingProfile.copy(strokeSize = newSize)
                    onProfileChanged(workingProfile)
                }
            )
        }
    }
}

@Composable
private fun StrokeStyleSection(
    selectedStyle: Int,
    onStyleSelected: (Int) -> Unit
) {
    Column {
        Text(
            text = "Stroke Style",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val styles = listOf(
                com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_CHARCOAL to com.wyldsoft.notes.R.drawable.ic_marker_pen,
                com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_FOUNTAIN to com.wyldsoft.notes.R.drawable.ic_pen_fountain,
                com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_MARKER to com.wyldsoft.notes.R.drawable.ic_pen_hard,
                com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_NEO_BRUSH to com.wyldsoft.notes.R.drawable.ic_pen_soft,
                com.onyx.android.sdk.pen.TouchHelper.STROKE_STYLE_PENCIL to com.wyldsoft.notes.R.drawable.ic_charcoal_pen
            )

            styles.forEach { (style, iconRes) ->
                val isSelected = style == selectedStyle
                val backgroundColor = if (isSelected) Color.LightGray else Color.Transparent
                val borderColor = if (isSelected) Color.Black else Color.Gray
                val borderWidth = if (isSelected) 2.dp else 1.dp

                IconButton(
                    onClick = { onStyleSelected(style) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(backgroundColor, RoundedCornerShape(4.dp))
                        .border(borderWidth, borderColor, RoundedCornerShape(4.dp))
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(iconRes),
                        contentDescription = getContentDescriptionForStyle(style),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSection(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    Column {
        Text(
            text = "Color",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        val colors = listOf(
            android.graphics.Color.BLACK,
            android.graphics.Color.WHITE,
            android.graphics.Color.RED,
            android.graphics.Color.GREEN,
            android.graphics.Color.BLUE,
            android.graphics.Color.YELLOW,
            android.graphics.Color.CYAN,
            android.graphics.Color.MAGENTA,
            android.graphics.Color.parseColor("#FF8C00"),
            android.graphics.Color.parseColor("#9932CC"),
            android.graphics.Color.parseColor("#8B4513"),
            android.graphics.Color.parseColor("#2E8B57"),
            android.graphics.Color.parseColor("#4682B4"),
            android.graphics.Color.parseColor("#D2691E"),
            android.graphics.Color.parseColor("#708090"),
            android.graphics.Color.parseColor("#FF1493")
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier.height(64.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(colors) { _, color ->
                val isSelected = color == selectedColor
                val borderWidth = if (isSelected) 3.dp else 1.dp
                val borderColor = if (isSelected) Color.Black else Color.Gray

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(color), RoundedCornerShape(2.dp))
                        .border(borderWidth, borderColor, RoundedCornerShape(2.dp))
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun SizeSection(
    currentSize: Float,
    onSizeChanged: (Float) -> Unit
) {
    Column {
        Text(
            text = "Stroke Size: ${String.format(Locale.getDefault(), "%.1f", currentSize)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = currentSize,
            onValueChange = onSizeChanged,
            valueRange = 0.5f..10.0f,
            modifier = Modifier.fillMaxWidth()
        )
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

// Extension function to create a copy of PenProfile
private fun PenProfile.copy(
    strokeSize: Float = this.strokeSize,
    strokeColor: Int = this.strokeColor,
    strokeStyle: Int = this.strokeStyle
): PenProfile {
    return PenProfile(strokeSize, strokeColor, strokeStyle)
}