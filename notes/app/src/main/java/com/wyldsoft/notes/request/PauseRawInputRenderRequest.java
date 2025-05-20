package com.wyldsoft.notes.request;

import com.wyldsoft.notes.PenManager;
import com.onyx.android.sdk.rx.RxRequest;

public class PauseRawInputRenderRequest extends RxRequest {
    private PenManager penManager;

    public PauseRawInputRenderRequest(PenManager penManager) {
        this.penManager = penManager;
    }

    @Override
    public void execute() throws Exception {
        penManager.setRawInputReaderEnable(false);
    }
}
