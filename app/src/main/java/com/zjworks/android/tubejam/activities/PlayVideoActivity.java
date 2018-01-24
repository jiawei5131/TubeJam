package com.zjworks.android.tubejam.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zjworks.android.tubejam.R;

public class PlayVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the video player fragment
        setContentView(R.layout.activity_play_video);
    }
}
