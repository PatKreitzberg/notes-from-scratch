package com.wyldsoft.notes.action;

import com.wyldsoft.notes.PenBundle;
import com.wyldsoft.notes.PenManager;
import com.onyx.android.sdk.rx.RxBaseAction;

public abstract class BaseAction<T> extends RxBaseAction<T> {

    protected PenBundle getDataBundle() {
        return PenBundle.getInstance();
    }

    protected PenManager getPenManager() {
        return getDataBundle().getPenManager();
    }

}
