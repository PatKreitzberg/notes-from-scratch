package com.wyldsoft.notes.scribble.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import com.wyldsoft.notes.R;
import com.onyx.android.sdk.pen.TouchHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileEditPopup {
    private static final String TAG = "ProfileEditPopup";

    public interface OnProfileChangedListener {
        void onProfileChanged(PenProfile profile);
        void onCancelled();
        void onPopupDismissed();
    }

    private PopupWindow popupWindow;
    private PenProfile originalProfile;
    private PenProfile workingProfile;
    private OnProfileChangedListener listener;
    private boolean isShowing = false;

    // UI components
    private List<ImageButton> styleButtons;
    private List<View> colorSquares;
    private SeekBar sizeSlider;
    private TextView sizeValue;

    // Available colors (16 distinct colors)
    private static final int[] COLORS = {
            Color.BLACK,           // #000000
            Color.WHITE,           // #FFFFFF
            Color.RED,             // #FF0000
            Color.GREEN,           // #00FF00
            Color.BLUE,            // #0000FF
            Color.YELLOW,          // #FFFF00
            Color.CYAN,            // #00FFFF
            Color.MAGENTA,         // #FF00FF
            Color.parseColor("#FF8C00"), // Dark Orange
            Color.parseColor("#9932CC"), // Dark Orchid
            Color.parseColor("#8B4513"), // Saddle Brown
            Color.parseColor("#2E8B57"), // Sea Green
            Color.parseColor("#4682B4"), // Steel Blue
            Color.parseColor("#D2691E"), // Chocolate
            Color.parseColor("#708090"), // Slate Gray
            Color.parseColor("#FF1493")  // Deep Pink
    };

    // Stroke styles and their corresponding icons and TouchHelper constants
    private static final int[] STROKE_STYLES = {
            TouchHelper.STROKE_STYLE_CHARCOAL,
            TouchHelper.STROKE_STYLE_FOUNTAIN,
            TouchHelper.STROKE_STYLE_MARKER,
            TouchHelper.STROKE_STYLE_NEO_BRUSH,
            TouchHelper.STROKE_STYLE_PENCIL
    };

    private static final int[] STROKE_ICONS = {
            R.drawable.ic_marker_pen,
            R.drawable.ic_pen_fountain,
            R.drawable.ic_pen_hard,
            R.drawable.ic_pen_soft,
            R.drawable.ic_charcoal_pen
    };

    public ProfileEditPopup(Context context, PenProfile profile, OnProfileChangedListener listener) {
        this.originalProfile = new PenProfile(profile.getStrokeSize(), profile.getStrokeColor(), profile.getStrokeStyle());
        this.workingProfile = new PenProfile(profile.getStrokeSize(), profile.getStrokeColor(), profile.getStrokeStyle());
        this.listener = listener;

        initPopup(context);
    }

    private void initPopup(Context context) {
        try {
            View contentView = LayoutInflater.from(context).inflate(R.layout.layout_profile_edit_popup, null);

            popupWindow = new PopupWindow(contentView,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            popupWindow.setWidth(800);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);

            // Add dismiss listener to handle cleanup - THIS IS THE KEY CHANGE
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    handleDismiss();
                }
            });

            // Add border
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.WHITE);
            background.setStroke(2, Color.BLACK);
            background.setCornerRadius(8f);
            popupWindow.setBackgroundDrawable(background);

            initViews(contentView);
            setupListeners();
            updateUI();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing popup", e);
        }
    }

    private void initViews(View contentView) {
        // Initialize stroke style buttons
        styleButtons = new ArrayList<>();
        styleButtons.add(contentView.findViewById(R.id.style_charcoal));
        styleButtons.add(contentView.findViewById(R.id.style_fountain));
        styleButtons.add(contentView.findViewById(R.id.style_marker));
        styleButtons.add(contentView.findViewById(R.id.style_neo_brush));
        styleButtons.add(contentView.findViewById(R.id.style_pencil));

        // Initialize color grid
        GridLayout colorGrid = contentView.findViewById(R.id.color_grid);
        colorSquares = new ArrayList<>();

        for (int i = 0; i < COLORS.length; i++) {
            View colorSquare = new View(contentView.getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 36;  // Slightly larger than before
            params.height = 36;
            params.setMargins(3, 3, 3, 3);
            colorSquare.setLayoutParams(params);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(COLORS[i]);
            drawable.setStroke(1, Color.GRAY);
            colorSquare.setBackground(drawable);

            final int colorIndex = i;
            colorSquare.setOnClickListener(v -> {
                workingProfile.setStrokeColor(COLORS[colorIndex]);
                updateColorSelection();
                if (listener != null) {
                    listener.onProfileChanged(workingProfile);
                }
                // Don't auto-dismiss on color change - let user continue editing
            });

            colorSquares.add(colorSquare);
            colorGrid.addView(colorSquare);
        }

        // Initialize size controls
        sizeSlider = contentView.findViewById(R.id.size_slider);
        sizeValue = contentView.findViewById(R.id.size_value);

        // Initialize cancel button with explicit dismiss
        Button cancelButton = contentView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> {
            Log.d(TAG, "Cancel button clicked");
            if (listener != null) {
                listener.onCancelled();
            }
            dismiss();
        });
    }

    private void setupListeners() {
        // Stroke style button listeners
        for (int i = 0; i < styleButtons.size(); i++) {
            final int styleIndex = i;
            styleButtons.get(i).setOnClickListener(v -> {
                workingProfile.setStrokeStyle(STROKE_STYLES[styleIndex]);
                updateStyleSelection();
                if (listener != null) {
                    listener.onProfileChanged(workingProfile);
                }
                // Don't auto-dismiss on style change - let user continue editing
            });
        }

        // Size slider listener
        sizeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float size = 0.5f + (progress / 95.0f) * 9.5f; // Map 0-95 to 0.5-10.0
                    workingProfile.setStrokeSize(size);
                    sizeValue.setText(String.format(Locale.getDefault(), "%.1f", size));
                    if (listener != null) {
                        listener.onProfileChanged(workingProfile);
                    }
                    // Don't auto-dismiss on size change
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateUI() {
        updateStyleSelection();
        updateColorSelection();
        updateSizeSelection();
    }

    private void updateStyleSelection() {
        for (int i = 0; i < styleButtons.size(); i++) {
            ImageButton button = styleButtons.get(i);

            // Create background programmatically instead of using drawable resources
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(4f);

            if (STROKE_STYLES[i] == workingProfile.getStrokeStyle()) {
                drawable.setColor(Color.LTGRAY);
                drawable.setStroke(2, Color.BLACK);
            } else {
                drawable.setColor(Color.TRANSPARENT);
                drawable.setStroke(1, Color.GRAY);
            }

            button.setBackground(drawable);
        }
    }

    private void updateColorSelection() {
        for (int i = 0; i < colorSquares.size(); i++) {
            View square = colorSquares.get(i);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(COLORS[i]);

            if (COLORS[i] == workingProfile.getStrokeColor()) {
                drawable.setStroke(3, Color.BLACK);
            } else {
                drawable.setStroke(1, Color.GRAY);
            }

            square.setBackground(drawable);
        }
    }

    private void updateSizeSelection() {
        float size = workingProfile.getStrokeSize();
        int progress = Math.round((size - 0.5f) / 9.5f * 95f); // Map 0.5-10.0 to 0-95
        sizeSlider.setProgress(progress);
        sizeValue.setText(String.format(Locale.getDefault(), "%.1f", size));
    }

    // Improved show method with state tracking
    public void showAsDropDown(View anchor) {
        try {
            if (popupWindow != null && !isShowing) {
                popupWindow.showAsDropDown(anchor, 0, 10);
                isShowing = true;
                Log.d(TAG, "Popup shown successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing popup", e);
            isShowing = false;
        }
    }

    // Improved dismiss method
    public void dismiss() {
        if (popupWindow != null && isShowing) {
            try {
                // First set the flag to prevent multiple dismissals
                isShowing = false;

                // Dismiss the popup window
                popupWindow.dismiss();

                // Clear the popup window reference to ensure cleanup
                popupWindow = null;

                Log.d(TAG, "Popup dismissed and cleaned up");
            } catch (Exception e) {
                Log.e(TAG, "Error dismissing popup", e);
                // Force cleanup even on error
                isShowing = false;
                popupWindow = null;
            }
        }
    }

    // UPDATED: Handle all dismissal scenarios - calls listener.onPopupDismissed()
    private void handleDismiss() {
        Log.d(TAG, "handleDismiss called");
        isShowing = false;

        // Call the popup dismissed listener - THIS IS THE KEY ADDITION
        if (listener != null) {
            listener.onPopupDismissed();
        }

        // Clear the popup window reference to ensure complete cleanup
        popupWindow = null;
    }

    // Add method to check if popup is showing
    public boolean isShowing() {
        return isShowing && popupWindow != null && popupWindow.isShowing();
    }

    // Force cleanup method for cases where normal dismiss doesn't work
    public void forceCleanup() {
        Log.d(TAG, "Force cleanup called");
        isShowing = false;
        if (popupWindow != null) {
            try {
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in force cleanup", e);
            } finally {
                popupWindow = null;
            }
        }
    }

    public static int getIconForStrokeStyle(int strokeStyle) {
        for (int i = 0; i < STROKE_STYLES.length; i++) {
            if (STROKE_STYLES[i] == strokeStyle) {
                return STROKE_ICONS[i];
            }
        }
        return R.drawable.ic_pen_fountain; // Default
    }
}