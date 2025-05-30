package com.wyldsoft.notes.scribble.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageButton;
import androidx.databinding.DataBindingUtil;

import com.wyldsoft.notes.R;
import com.wyldsoft.notes.databinding.ActivityNotesBinding;
import com.wyldsoft.notes.scribble.broadcast.GlobalDeviceReceiver;
import com.wyldsoft.notes.scribble.request.RendererToScreenRequest;
import com.wyldsoft.notes.scribble.util.TouchUtils;

import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.onyx.android.sdk.rx.RxManager;
import java.util.ArrayList;
import java.util.List;

public class ScribbleNotes extends AppCompatActivity {
    private static final String TAG = ScribbleNotes.class.getSimpleName();
    private static final String TAG_PROFILE = "profile";

    private ActivityNotesBinding binding;
    private GlobalDeviceReceiver deviceReceiver = new GlobalDeviceReceiver();
    private RxManager rxManager;
    private TouchHelper touchHelper;
    private Paint paint = new Paint();
    private Bitmap bitmap;
    private Canvas canvas;

    // Pen Profile management
    private PenProfile currentPenProfile;
    private List<PenProfile> penProfiles;
    private List<ImageButton> toolbarButtons;
    private int selectedButtonIndex = 0;

    private ProfileEditPopup currentPopup;
    private boolean isPopupShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notes);
        deviceReceiver.enable(this, true);
        binding.setActivityNotes(this);

        initPenProfiles();
        initToolbar();
        initPaint();
        initSurfaceView();
        initReceiver();
    }

    @Override
    protected void onResume() {
        if (touchHelper != null) {
            touchHelper.setRawDrawingEnabled(true);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (touchHelper != null) {
            touchHelper.closeRawDrawing();
        }
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        deviceReceiver.enable(this, false);
        super.onDestroy();
    }

    private void initPenProfiles() {
        penProfiles = new ArrayList<>();
        penProfiles.add(PenProfile.createCharcoalProfile());
        penProfiles.add(PenProfile.createFountainProfile());
        penProfiles.add(PenProfile.createMarkerProfile());
        penProfiles.add(PenProfile.createNeoBrushProfile());
        penProfiles.add(PenProfile.createPencilProfile());

        // Set default profile
        currentPenProfile = penProfiles.get(0);
    }

    private void initToolbar() {
        toolbarButtons = new ArrayList<>();
        toolbarButtons.add(binding.buttonCharcoal);
        toolbarButtons.add(binding.buttonFountain);
        toolbarButtons.add(binding.buttonMarker);
        toolbarButtons.add(binding.buttonNeoBrush);
        toolbarButtons.add(binding.buttonPencil);

        // Set up button backgrounds and click listeners
        for (int i = 0; i < toolbarButtons.size(); i++) {
            final int index = i;
            ImageButton button = toolbarButtons.get(i);
            PenProfile profile = penProfiles.get(i);

            // Set initial appearance
            updateButtonForProfile(button, profile, i == selectedButtonIndex);

            button.setOnClickListener(v -> selectPenProfile(index));
        }
    }

    private void selectPenProfile(int index) {
        Log.d("profile", "selectPenProfile index: " + index);

        if (index < 0 || index >= penProfiles.size()) {
            Log.w("profile", "Invalid profile index: " + index);
            return;
        }

        // Check if clicking the already selected profile
        if (selectedButtonIndex == index) {
            // Show edit popup
            Log.d("profile", "show edit popup for index " + index);
            showProfileEditPopup(index);
            return;
        }

        // Switch to different profile
        selectedButtonIndex = index;
        currentPenProfile = penProfiles.get(index);

        // Update UI and pen settings
        updateAllButtonAppearances();
        updatePenSettings();

        Log.d(TAG, "Selected pen profile: " + index + ", Color: " + currentPenProfile.getStrokeColor() +
                ", Style: " + currentPenProfile.getStrokeStyle());
    }

    private void updateAllButtonAppearances() {
        for (int i = 0; i < toolbarButtons.size(); i++) {
            ImageButton button = toolbarButtons.get(i);
            PenProfile profile = penProfiles.get(i);
            updateButtonForProfile(button, profile, i == selectedButtonIndex);
        }
    }

    private void showProfileEditPopup(int profileIndex) {
        Log.d(TAG_PROFILE, "showProfileEditPopup for index: " + profileIndex);

        // Dismiss any existing popup first
        dismissCurrentPopup();

        try {
            // Pause drawing while popup is shown
            pauseDrawing();

            PenProfile profileToEdit = penProfiles.get(profileIndex);
            ImageButton anchorButton = toolbarButtons.get(profileIndex);

            currentPopup = new ProfileEditPopup(this, profileToEdit, new ProfileEditPopupListener(profileIndex));
            isPopupShowing = true;

            currentPopup.showAsDropDown(anchorButton);

            Log.d(TAG_PROFILE, "Profile edit popup shown for index: " + profileIndex);

        } catch (Exception e) {
            Log.e(TAG_PROFILE, "Error showing profile edit popup", e);
            // Cleanup on error
            cleanupPopup();
            resumeDrawing();
        }
    }

    private void dismissCurrentPopup() {
        if (currentPopup != null) {
            try {
                Log.d(TAG_PROFILE, "Dismissing current popup");
                currentPopup.dismiss(); // This should trigger the dismiss listener
                resumeDrawing();
                cleanupPopup();
            } catch (Exception e) {
                Log.e(TAG_PROFILE, "Error dismissing popup", e);
                // Force cleanup even if dismiss fails
                cleanupPopup();
                resumeDrawing();
            }
        }
    }

    private void cleanupPopup() {
        currentPopup = null;
        isPopupShowing = false;
    }

    private void pauseDrawing() {
        if (touchHelper != null) {
            touchHelper.setRawDrawingEnabled(false);
            Log.d(TAG_PROFILE, "Drawing paused");
        }
    }

    private void resumeDrawing() {
        if (touchHelper != null && !isPopupShowing) {
            touchHelper.setRawDrawingEnabled(true);
            Log.d(TAG_PROFILE, "Drawing resumed");
        }
    }

    private class ProfileEditPopupListener implements ProfileEditPopup.OnProfileChangedListener {
        private final int profileIndex;

        public ProfileEditPopupListener(int profileIndex) {
            this.profileIndex = profileIndex;
        }

        @Override
        public void onProfileChanged(PenProfile profile) {
            Log.d(TAG_PROFILE, "Profile changed for index: " + profileIndex);

            // Update the profile in the list
            penProfiles.set(profileIndex, profile);
            currentPenProfile = profile;

            // Update UI
            ImageButton button = toolbarButtons.get(profileIndex);
            updateButtonForProfile(button, profile, true);
            updatePenSettings();

            Log.d(TAG_PROFILE, "Profile modified: " + profileIndex +
                    ", Color: " + profile.getStrokeColor() +
                    ", Style: " + profile.getStrokeStyle() +
                    ", Size: " + profile.getStrokeSize());
        }

        @Override
        public void onCancelled() {
            Log.d(TAG_PROFILE, "Profile edit cancelled or dismissed");
            handlePopupDismissal();
        }

        @Override
        public void onPopupDismissed() {
            Log.d(TAG_PROFILE, "Popup dismissed");
            handlePopupDismissal();
        }

        // Handle popup dismissal (both cancel and completion)
        private void handlePopupDismissal() {
            Log.d(TAG_PROFILE, "handlePopupDismissal");
            cleanupPopup();
            resumeDrawing();
        }
    }


    @Override
    protected void onPause() {
        dismissCurrentPopup();
        super.onPause();
    }

    // Add method to handle back press
    @Override
    public void onBackPressed() {
        if (isPopupShowing) {
            dismissCurrentPopup();
            resumeDrawing();
        } else {
            super.onBackPressed();
        }
    }


    private void updateButtonForProfile(ImageButton button, PenProfile profile, boolean isSelected) {
        // Update background color
        updateButtonAppearance(button, profile.getStrokeColor(), isSelected);

        // Update icon based on stroke style
        int iconRes = ProfileEditPopup.getIconForStrokeStyle(profile.getStrokeStyle());
        button.setImageResource(iconRes);
    }

    private void updatePenSettings() {
        // Update touch helper settings
        if (touchHelper != null) {
            touchHelper.setStrokeColor(currentPenProfile.getStrokeColor());
            touchHelper.setStrokeStyle(currentPenProfile.getStrokeStyle());
            touchHelper.setStrokeWidth(currentPenProfile.getStrokeSize());
        }

        // Update paint
        paint.setColor(currentPenProfile.getStrokeColor());
        paint.setStrokeWidth(currentPenProfile.getStrokeSize());
    }

    private void updateButtonAppearance(ImageButton button, int backgroundColor, boolean isSelected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(backgroundColor);
        drawable.setCornerRadius(8f);

        if (isSelected) {
            drawable.setStroke(4, Color.BLACK);
        } else {
            drawable.setStroke(1, Color.GRAY);
        }

        button.setBackground(drawable);
    }

    public RxManager getRxManager() {
        if (rxManager == null) {
            rxManager = RxManager.Builder.sharedSingleThreadManager();
        }
        return rxManager;
    }

    public void renderToScreen(SurfaceView surfaceView, Bitmap bitmap) {
        getRxManager().enqueue(new RendererToScreenRequest(surfaceView, bitmap), null);
    }

    private void initPaint() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(currentPenProfile.getStrokeColor());
        paint.setStrokeWidth(currentPenProfile.getStrokeSize());
    }

    private void initSurfaceView() {
        touchHelper = TouchHelper.create(binding.surfaceview, callback);

        binding.surfaceview.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (cleanSurfaceView()) {
                binding.surfaceview.removeOnLayoutChangeListener(this::onLayoutChange);
            }

            android.graphics.Rect limit = new android.graphics.Rect();
            binding.surfaceview.getLocalVisibleRect(limit);
            touchHelper.setStrokeWidth(currentPenProfile.getStrokeSize())
                    .setLimitRect(limit, new ArrayList<>())
                    .openRawDrawing();

            // Set initial pen profile settings
            touchHelper.setStrokeStyle(currentPenProfile.getStrokeStyle());
            touchHelper.setStrokeColor(currentPenProfile.getStrokeColor());

            // Automatically enable drawing - no button press required
            touchHelper.setRawDrawingEnabled(true);
            touchHelper.setRawDrawingRenderEnabled(true);

            binding.surfaceview.addOnLayoutChangeListener(this::onLayoutChange);
        });

        final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                cleanSurfaceView();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                holder.removeCallback(this);
            }
        };
        binding.surfaceview.getHolder().addCallback(surfaceCallback);
    }

    private void onLayoutChange(android.view.View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        // Layout change handling
    }

    private void initReceiver() {
        deviceReceiver.setSystemNotificationPanelChangeListener(open -> {
            if (touchHelper != null) {
                touchHelper.setRawDrawingEnabled(!open);
                renderToScreen(binding.surfaceview, bitmap);
            }
        }).setSystemScreenOnListener(() -> renderToScreen(binding.surfaceview, bitmap));
    }

    private boolean cleanSurfaceView() {
        if (binding.surfaceview.getHolder() == null) {
            return false;
        }
        Canvas canvas = binding.surfaceview.getHolder().lockCanvas();
        if (canvas == null) {
            return false;
        }
        canvas.drawColor(Color.WHITE);
        binding.surfaceview.getHolder().unlockCanvasAndPost(canvas);
        return true;
    }

    private RawInputCallback callback = new RawInputCallback() {

        @Override
        public void onBeginRawDrawing(boolean b, TouchPoint touchPoint) {
            //Log.D(TAG, "onBeginRawDrawing");
            TouchUtils.disableFingerTouch(getApplicationContext());
        }

        @Override
        public void onEndRawDrawing(boolean b, TouchPoint touchPoint) {
            //Log.D(TAG, "onEndRawDrawing");
            TouchUtils.enableFingerTouch(getApplicationContext());
        }

        @Override
        public void onRawDrawingTouchPointMoveReceived(TouchPoint touchPoint) {
            //Log.d(TAG, "onRawDrawingTouchPointMoveReceived");
        }

        @Override
        public void onRawDrawingTouchPointListReceived(TouchPointList touchPointList) {
            //Log.d(TAG, "onRawDrawingTouchPointListReceived");
            drawScribbleToBitmap(touchPointList.getPoints());
        }

        @Override
        public void onBeginRawErasing(boolean b, TouchPoint touchPoint) {
            //Log.d(TAG, "onBeginRawErasing");
        }

        @Override
        public void onEndRawErasing(boolean b, TouchPoint touchPoint) {
            //Log.D(TAG, "onEndRawErasing");
        }

        @Override
        public void onRawErasingTouchPointMoveReceived(TouchPoint touchPoint) {
            //Log.D(TAG, "onRawErasingTouchPointMoveReceived");
        }

        @Override
        public void onRawErasingTouchPointListReceived(TouchPointList touchPointList) {
            //Log.D(TAG, "onRawErasingTouchPointListReceived");
        }
    };

    private void drawScribbleToBitmap(List<TouchPoint> list) {
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(binding.surfaceview.getWidth(),
                    binding.surfaceview.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
        }

        // Create a new Paint object with the current pen profile settings
        Paint bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setStyle(Paint.Style.STROKE);
        bitmapPaint.setColor(currentPenProfile.getStrokeColor());
        bitmapPaint.setStrokeWidth(currentPenProfile.getStrokeSize());
        bitmapPaint.setStrokeCap(Paint.Cap.ROUND);
        bitmapPaint.setStrokeJoin(Paint.Join.ROUND);

        // Use fountain pen style drawing
        Path path = new Path();
        PointF prePoint = new PointF(list.get(0).x, list.get(0).y);
        path.moveTo(prePoint.x, prePoint.y);
        for (TouchPoint point : list) {
            path.quadTo(prePoint.x, prePoint.y, point.x, point.y);
            prePoint.x = point.x;
            prePoint.y = point.y;
        }
        canvas.drawPath(path, bitmapPaint);
    }

    // Helper method to get current pen profile
    public PenProfile getCurrentPenProfile() {
        return currentPenProfile;
    }
}