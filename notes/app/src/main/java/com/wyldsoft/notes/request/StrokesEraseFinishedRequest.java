package com.wyldsoft.notes.request;

import androidx.annotation.NonNull;

import com.wyldsoft.notes.PenManager;
import com.wyldsoft.notes.data.InteractiveMode;

public class StrokesEraseFinishedRequest extends BaseRequest {

    public StrokesEraseFinishedRequest(@NonNull PenManager penManager) {
        super(penManager);
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
        penManager.activeRenderMode(InteractiveMode.SCRIBBLE);
        penManager.getRenderContext().eraseArgs = null;
        penManager.renderToBitmap(penManager.getDrawShape());
    }
}
