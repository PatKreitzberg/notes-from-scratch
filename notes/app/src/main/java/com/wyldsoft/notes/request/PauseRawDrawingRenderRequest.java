package com.wyldsoft.notes.request;

import com.wyldsoft.notes.PenManager;
import com.onyx.android.sdk.rx.RxRequest;

public class PauseRawDrawingRenderRequest extends RxRequest {
    private PenManager penManager;

    public PauseRawDrawingRenderRequest(PenManager penManager) {
        this.penManager = penManager;
    }

    @Override
    public void execute() throws Exception {
        penManager.setRawDrawingRenderEnabled(false);
    }
}
