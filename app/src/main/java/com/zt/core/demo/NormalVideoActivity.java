package com.zt.core.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.zt.core.base.BasePlayer;
import com.zt.core.base.PlayerConfig;
import com.zt.core.player.AndroidPlayer;
import com.zt.core.view.StandardVideoView;
import com.zt.exoplayer.GoogleExoPlayer;
import com.zt.ijkplayer.IjkPlayer;

public class NormalVideoActivity extends AppCompatActivity {

    private Sample sample;

    private StandardVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_normal_video);

        sample = (Sample) getIntent().getSerializableExtra("sample");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Normal Video");

        videoView = findViewById(R.id.video_view);
        videoView.setTitle(sample.title);

        switch (sample.fileType) {
            case "url":
                videoView.setVideoPath(sample.path);
                break;
        }

        BasePlayer player;
        switch (sample.player) {
            case 0:
                player = new AndroidPlayer(this);
                break;
            case 1:
                player = new IjkPlayer(this);
                break;
            default:
                player = new GoogleExoPlayer(this);
                break;
        }

        int renderType = sample.renderType == 0 ? PlayerConfig.RENDER_TEXTURE_VIEW : PlayerConfig.RENDER_SURFACE_VIEW;

        //设置全屏策略，设置视频渲染界面类型,设置是否循环播放，设置自定义播放器
        PlayerConfig playerConfig = new PlayerConfig.Builder()
                .fullScreenMode(sample.fullscreenMode)
                .renderType(renderType)
                .looping(sample.looping)
                .player(player)  //IjkPlayer,GoogleExoPlayer 需添加对应的依赖
                .build();
        videoView.setPlayerConfig(playerConfig);

        //设置是否支持手势调节音量, 默认支持
        videoView.setSupportVolume(sample.volumeSupport);

        //设置是否支持手势调节亮度，默认支持
        videoView.setSupportBrightness(sample.brightnessSupport);

        //设置是否支持手势调节播放进度，默认支持
        videoView.setSupportSeek(sample.seekSupport);

        //设置是否支持锁定屏幕，默认全屏的时候支持
        videoView.setSupportLock(true);

        videoView.start();

        setDescView();
    }

    private void setDescView() {
        TextView descTextView = findViewById(R.id.desc);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Player: ");
        stringBuilder.append(sample.player == 0 ? "Android MediaPlayer" : sample.player == 1 ? "IjkPlayer" : "ExoPlayer");
        stringBuilder.append("\n");

        stringBuilder.append("Render: ");
        stringBuilder.append(sample.renderType == 0 ? "TextureView" : "SurfaceView");
        stringBuilder.append("\n");

        stringBuilder.append("Video:");
        stringBuilder.append(sample.path);
        stringBuilder.append("\n");

        stringBuilder.append("FullScreenMode: ");
        stringBuilder.append(sample.fullscreenMode == 0 ? "landscape fullscreen"
                :  sample.fullscreenMode == 1 ? "portrait fullscreen" : "auto orientation fullscreen");
        stringBuilder.append("\n");


        descTextView.setText(stringBuilder.toString());
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
