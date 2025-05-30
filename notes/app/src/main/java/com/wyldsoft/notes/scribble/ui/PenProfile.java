package com.wyldsoft.notes.scribble.ui;

import android.graphics.Color;
import com.onyx.android.sdk.pen.TouchHelper;

public class PenProfile {
    private float strokeSize;
    private int strokeColor;
    private int strokeStyle;

    public PenProfile(float strokeSize, int strokeColor, int strokeStyle) {
        this.strokeSize = strokeSize;
        this.strokeColor = strokeColor;
        this.strokeStyle = strokeStyle;
    }

    // Getters
    public float getStrokeSize() {
        return strokeSize;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public int getStrokeStyle() {
        return strokeStyle;
    }

    // Setters
    public void setStrokeSize(float strokeSize) {
        this.strokeSize = strokeSize;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public void setStrokeStyle(int strokeStyle) {
        this.strokeStyle = strokeStyle;
    }

    // Static factory methods for default profiles
    public static PenProfile createCharcoalProfile() {
        return new PenProfile(3.0f, Color.parseColor("#8B4513"), TouchHelper.STROKE_STYLE_CHARCOAL); // Saddle Brown
    }

    public static PenProfile createFountainProfile() {
        return new PenProfile(3.0f, Color.parseColor("#000080"), TouchHelper.STROKE_STYLE_FOUNTAIN); // Navy Blue
    }

    public static PenProfile createMarkerProfile() {
        return new PenProfile(3.0f, Color.parseColor("#FF6347"), TouchHelper.STROKE_STYLE_MARKER); // Tomato Red
    }

    public static PenProfile createNeoBrushProfile() {
        return new PenProfile(3.0f, Color.parseColor("#32CD32"), TouchHelper.STROKE_STYLE_NEO_BRUSH); // Lime Green
    }

    public static PenProfile createPencilProfile() {
        return new PenProfile(3.0f, Color.parseColor("#4B0082"), TouchHelper.STROKE_STYLE_PENCIL); // Indigo
    }
}
