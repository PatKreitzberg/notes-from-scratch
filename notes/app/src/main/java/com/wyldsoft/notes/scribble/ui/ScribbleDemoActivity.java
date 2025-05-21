package com.wyldsoft.notes.scribble.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.wyldsoft.notes.R;
import com.wyldsoft.notes.databinding.ActivitySribbleDemoBinding;

/**
 * Created by seeksky on 2018/4/26.
 */

public class ScribbleDemoActivity extends AppCompatActivity {
    private ActivitySribbleDemoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sribble_demo);
        binding.setActivitySribble(this);
    }

    public void button_scribble_touch_helper(View view) {
        go(ScribbleTouchHelperDemoActivity.class);
    }

    public void button_surfaceview_stylus_scribble(View view) {
        go(ScribbleTouchHelperDemoActivity.class);
    }

    public void button_webview_stylus_scribble(View view) {
        go(ScribbleWebViewDemoActivity.class);
    }

    public void button_notes(View view) {
        go(ScribbleNotes.class);
    }

    public void button_move_erase_scribble(View view) {
        go(ScribbleMoveEraserDemoActivity.class);
    }

    public void button_multiple_scribble(View view) {
        go(ScribbleMultipleScribbleViewActivity.class);
    }

    public void button_pen_up_refresh(View view) {
        go(ScribblePenUpRefreshDemoActivity.class);
    }

    public void button_epd_controller(View view) {
        go(ScribbleEpdControllerDemoActivity.class);
    }

    public void gotoScribbleFingerTouchDemo(View view) {
        go(ScribbleFingerTouchDemoActivity.class);
    }

    private void go(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }
}
