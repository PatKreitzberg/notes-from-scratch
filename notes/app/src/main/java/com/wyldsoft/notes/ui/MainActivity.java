package com.wyldsoft.notes.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.wyldsoft.notes.R;
import com.wyldsoft.notes.databinding.ActivityMainBinding;
import com.wyldsoft.notes.scribble.ui.ScribbleNotes;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.buttonScribbleDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(ScribbleNotes.class);
            }
        });
    }

    private void go(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

}
