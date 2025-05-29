package com.wyldsoft.notes.render;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceView;

import com.wyldsoft.notes.helper.RendererHelper;
import com.wyldsoft.notes.util.RendererUtils;
import com.onyx.android.sdk.utils.CanvasUtils;
import com.onyx.android.sdk.utils.RectUtils;

public class PartialRefreshRenderer extends BaseRenderer {

    @Override
    public void renderToScreen(SurfaceView surfaceView, RendererHelper.RenderContext renderContext) {
        if (surfaceView == null) {
            return;
        }
        Rect renderRect = RectUtils.toRect(renderContext.clipRect);
        Rect viewRect = RendererUtils.checkSurfaceView(surfaceView);
        Canvas canvas = lockHardwareCanvas(surfaceView.getHolder(), renderRect);
        if (canvas == null) {
            return;
        }
        try {
            CanvasUtils.clipRect(canvas, renderRect);
            RendererUtils.renderBackground(canvas, viewRect);
            drawRendererContent(renderContext.bitmap, canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            surfaceView.getHolder().unlockCanvasAndPost(canvas);
        }
    }

}
