package com.zt.simplevideoplayer;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.zt.simplevideo.view.StandardVideoView;

public class NormalVideoActivity extends AppCompatActivity {


    private StandardVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_normal_video);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Normal Video");

        videoView = findViewById(R.id.video_view);
        videoView.setVideoPath("http://mirror.aarnet.edu.au/pub/TED-talks/AlexLaskey_2013.mp4");

        //设置全屏策略，默认横屏全屏
        videoView.setFullScreenMode(StandardVideoView.AUTO_FULLSCREEN_MODE);

        //设置是否支持手势调节音量, 默认支持
        videoView.setSupportVolume(true);

        //设置是否支持手势调节亮度，默认支持
        videoView.setSupportBrightness(true);

        //设置是否支持手势调节播放进度，默认支持
        videoView.setSupportSeek(true);

        //设置是否支持锁定屏幕，默认全屏的时候支持
        videoView.setSupportLock(true);

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
