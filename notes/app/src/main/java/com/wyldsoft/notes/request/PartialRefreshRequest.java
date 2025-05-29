package com.wyldsoft.notes.request;

import android.graphics.RectF;

import com.wyldsoft.notes.PenManager;
import com.wyldsoft.notes.data.InteractiveMode;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.api.device.epd.UpdateMode;
import com.onyx.android.sdk.rx.RxRequest;

public class PartialRefreshRequest extends RxRequest {
    private final RectF refreshRect;
    private final PenManager penManager;

    public PartialRefreshRequest(PenManager penManager, RectF refreshRect) {
        this.penManager = penManager;
        this.refreshRect = refreshRect;
    }

    @Override
    public void execute() throws Exception {
        try {
            EpdController.setViewDefaultUpdateMode(penManager.getSurfaceView(), UpdateMode.HAND_WRITING_REPAINT_MODE);
            penManager.getRenderContext().clipRect = refreshRect;
            penManager.activeRenderMode(InteractiveMode.SCRIBBLE_PARTIAL_REFRESH);
            penManager.renderToScreen();
        } finally {
            EpdController.resetViewUpdateMode(penManager.getSurfaceView());
        }
    }
}
