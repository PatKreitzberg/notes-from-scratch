package com.wyldsoft.notes.request;

import androidx.annotation.NonNull;

import com.wyldsoft.notes.PenManager;
import com.wyldsoft.notes.data.ShapeFactory;

public class StrokeStyleChangeRequest extends BaseRequest {
    private int shapeType;
    private int texture;

    public StrokeStyleChangeRequest(@NonNull PenManager penManager) {
        super(penManager);
    }

    public StrokeStyleChangeRequest setShapeType(int shapeType) {
        this.shapeType = shapeType;
        return this;
    }

    public StrokeStyleChangeRequest setTexture(int texture) {
        this.texture = texture;
        return this;
    }

    @Override
    public void execute(PenManager penManager) throws Exception {
        getPenManager().setStrokeStyle(ShapeFactory.getStrokeStyle(shapeType, texture));
    }
}
