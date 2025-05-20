package com.wyldsoft.notes.request;

import androidx.annotation.NonNull;

import com.wyldsoft.notes.PenManager;

public class StrokeColorChangeRequest extends BaseRequest {
    private int color;

    public StrokeColorChangeRequest(@NonNull PenManager penManager) {
        super(penManager);
    }

    public StrokeColorChangeRequest setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
        getPenManager().setStrokeColor(color);
    }
}
