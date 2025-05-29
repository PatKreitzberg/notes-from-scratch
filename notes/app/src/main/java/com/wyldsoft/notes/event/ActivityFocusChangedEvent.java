package com.wyldsoft.notes.event;

public class ActivityFocusChangedEvent {
    public boolean hasFocus;

    public ActivityFocusChangedEvent(boolean hasFocus) {
        this.hasFocus = hasFocus;
    }
}