package com.wyldsoft.notes.action;

import com.wyldsoft.notes.PenBundle;
import com.wyldsoft.notes.PenManager;
import com.onyx.android.sdk.rx.RxBaseAction;
import com.onyx.android.sdk.rx.RxRequest;
import com.onyx.android.sdk.utils.ResManager;

import io.reactivex.Observable;

public class CommonPenAction<T extends RxRequest> extends RxBaseAction<T> {
    private final T request;

    public CommonPenAction(T request) {
        this.request = request;
    }

    @Override
    protected Observable<T> create() {
        return getPenManager().createObservable()
                .map(o -> executeRequest())
                .observeOn(getMainUIScheduler());
    }

    private T executeRequest() throws Exception {
        request.setContext(ResManager.getAppContext());
        request.execute();
        return request;
    }

    public PenBundle getDataBundle() {
        return PenBundle.getInstance();
    }

    public PenManager getPenManager() {
        return getDataBundle().getPenManager();
    }

}
