package com.zt.core.demo;

import androidx.annotation.LayoutRes;
import android.view.KeyEvent;

import com.zt.core.view.StandardVideoView;

public class NormalVideoActivity extends BaseDemoActivity {

    protected StandardVideoView videoView;

    @Override
    protected void initView() {
        setActionBarTitle();

        videoView = findViewById(R.id.video_view);

        initPlayerView(videoView);

        initDescView(findViewById(R.id.desc));

        videoView.start();
    }


    protected void setActionBarTitle() {
        getSupportActionBar().setTitle("Video Play");
    }

    @Override
    protected @LayoutRes
    int getLayoutId() {
        return R.layout.aty_normal_video;
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
