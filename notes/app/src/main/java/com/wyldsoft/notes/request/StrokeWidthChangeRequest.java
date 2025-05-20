package com.wyldsoft.notes.request;

import androidx.annotation.NonNull;

import com.wyldsoft.notes.PenManager;

public class StrokeWidthChangeRequest extends BaseRequest {
    private float width;

    public StrokeWidthChangeRequest(@NonNull PenManager penManager) {
        super(penManager);
    }

    public StrokeWidthChangeRequest setWidth(float width) {
        this.width = width;
        return this;
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
        getPenManager().setStrokeWidth(width);
    }
}
