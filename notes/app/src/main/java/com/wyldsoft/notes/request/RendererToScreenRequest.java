package com.wyldsoft.notes.request;

import androidx.annotation.NonNull;

import com.wyldsoft.notes.PenManager;

public class RendererToScreenRequest extends BaseRequest {

    public RendererToScreenRequest(@NonNull PenManager noteManager) {
        super(noteManager);
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
    }

}
