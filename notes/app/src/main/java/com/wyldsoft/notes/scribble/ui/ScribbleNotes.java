package com.wyldsoft.notes.scribble.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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

    private ActivityNotesBinding binding;
    private GlobalDeviceReceiver deviceReceiver = new GlobalDeviceReceiver();
    private RxManager rxManager;
    private TouchHelper touchHelper;
    private Paint paint = new Paint();
    private Bitmap bitmap;
    private Canvas canvas;

    private final float STROKE_WIDTH = 3.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notes);
        deviceReceiver.enable(this, true);
        binding.setActivityNotes(this);

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
    protected void onPause() {
        if (touchHelper != null) {
            touchHelper.setRawDrawingEnabled(false);
        }
        super.onPause();
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
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(STROKE_WIDTH);
    }

    private void initSurfaceView() {
        touchHelper = TouchHelper.create(binding.surfaceview, callback);

        binding.surfaceview.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (cleanSurfaceView()) {
                binding.surfaceview.removeOnLayoutChangeListener(this::onLayoutChange);
            }

            android.graphics.Rect limit = new android.graphics.Rect();
            binding.surfaceview.getLocalVisibleRect(limit);
            touchHelper.setStrokeWidth(STROKE_WIDTH)
                    .setLimitRect(limit, new ArrayList<>())
                    .openRawDrawing();
            touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_FOUNTAIN);

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
            Log.d(TAG, "onBeginRawDrawing");
            TouchUtils.disableFingerTouch(getApplicationContext());
        }

        @Override
        public void onEndRawDrawing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onEndRawDrawing");
            TouchUtils.enableFingerTouch(getApplicationContext());
        }

        @Override
        public void onRawDrawingTouchPointMoveReceived(TouchPoint touchPoint) {
            Log.d(TAG, "onRawDrawingTouchPointMoveReceived");
        }

        @Override
        public void onRawDrawingTouchPointListReceived(TouchPointList touchPointList) {
            Log.d(TAG, "onRawDrawingTouchPointListReceived");
            drawScribbleToBitmap(touchPointList.getPoints());
        }

        @Override
        public void onBeginRawErasing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onBeginRawErasing");
        }

        @Override
        public void onEndRawErasing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onEndRawErasing");
        }

        @Override
        public void onRawErasingTouchPointMoveReceived(TouchPoint touchPoint) {
            Log.d(TAG, "onRawErasingTouchPointMoveReceived");
        }

        @Override
        public void onRawErasingTouchPointListReceived(TouchPointList touchPointList) {
            Log.d(TAG, "onRawErasingTouchPointListReceived");
        }
    };

    private void drawScribbleToBitmap(List<TouchPoint> list) {
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(binding.surfaceview.getWidth(),
                    binding.surfaceview.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
        }

        // Use fountain pen style drawing
        Path path = new Path();
        PointF prePoint = new PointF(list.get(0).x, list.get(0).y);
        path.moveTo(prePoint.x, prePoint.y);
        for (TouchPoint point : list) {
            path.quadTo(prePoint.x, prePoint.y, point.x, point.y);
            prePoint.x = point.x;
            prePoint.y = point.y;
        }
        canvas.drawPath(path, paint);
    }
}