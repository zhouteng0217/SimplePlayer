package com.zt.simplevideoplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.zt.simplevideo.view.StandardVideoView;

public class NormalVideoActivity extends AppCompatActivity {


    private StandardVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_normal_video);

        videoView = findViewById(R.id.video_view);
        videoView.setVideoPath("http://mirror.aarnet.edu.au/pub/TED-talks/AlexLaskey_2013.mp4");

        //设置全屏策略
        videoView.setFullScreenMode(StandardVideoView.AUTO_FULLSCREEN_MODE);
        videoView.setSupportVolume(false);
        videoView.setSupportBrightness(false);
        videoView.setSupportSeek(false);

        videoView.start();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (videoView.onBackKeyPressed()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }
}
