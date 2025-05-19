package com.wyldsoft.notes.ui

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PopupChangeAction(private val show: Boolean) {
    private val disposables = CompositeDisposable()

    fun execute() {
        val observable = Observable.just(show)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        disposables.add(
            observable.subscribe { isShowing ->
                // Pause/resume drawing based on popup state
                // This is where we would integrate with PenManager to pause drawing
                // while the popup is showing
            }
        )
    }
}