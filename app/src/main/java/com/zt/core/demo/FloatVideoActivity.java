package com.zt.core.demo;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.zt.core.base.ITinyVideoView;
import com.zt.core.base.RenderContainerView;
import com.zt.core.player.FloatVideoManager;
import com.zt.core.view.FloatVideoView;
import com.zt.core.view.StandardVideoView;

/**
 * Created by zhouteng on 2019-09-14
 */
public class FloatVideoActivity extends BaseDemoActivity implements CompoundButton.OnCheckedChangeListener {

    public static final int REQUEST_DRAWOVERLAYS_CODE = 10000;

    private StandardVideoView videoView;
    private CheckBox checkBox;

    @Override
    protected void initView() {
        FrameLayout container = findViewById(R.id.video_view_container);

        //由于要将RenderContainerView层添加到window层，为防止内存泄露，重写了newRenderContainerView, 用application的context来构建
        videoView = new StandardVideoView(this) {
            @Override
            protected RenderContainerView newRenderContainerView() {
                return new RenderContainerView(getApplicationContext());
            }
        };

        container.addView(videoView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initPlayerView(videoView);
        initDescView(findViewById(R.id.desc));

        checkBox = findViewById(R.id.checkbox);
        initCheckBox();
    }

    private void initCheckBox() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                checkBox.setChecked(false);
            } else {
                checkBox.setChecked(true);
            }
        } else {
            checkBox.setChecked(true);
        }
        checkBox.setText(checkBox.isChecked() ? "退出后，将开启小窗口模式" : "请先开启悬浮窗权限");
        checkBox.setEnabled(!checkBox.isChecked());
        checkBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_DRAWOVERLAYS_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_DRAWOVERLAYS_CODE) {
            initCheckBox();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //只销毁播放器UI控制层上面相关要销毁的要素，不销毁播放器和渲染界面实例，用于添加到window层
        videoView.destroyVideoView();

        startFloatVideoView();
    }

    private void startFloatVideoView() {
        //注意使用application的context构建，防止内存泄露
        FloatVideoView floatVideoView = new FloatVideoView(this.getApplicationContext());

        ITinyVideoView.LayoutParams layoutParams = new ITinyVideoView.LayoutParams(600, 336);
        layoutParams.x = 20;
        layoutParams.y = 20;
        floatVideoView.setFloatVideoLayoutParams(layoutParams);

        //将正在播放的VideoView的RenderContainer层从原来界面剥离出来，添加到自定义的悬浮窗VideoView上
        RenderContainerView renderContainerView = videoView.getRenderContainerView();
        ((ViewGroup) renderContainerView.getParent()).removeView(renderContainerView);

        floatVideoView.addRenderContainer(renderContainerView);

        FloatVideoManager.getInstance().startFloatVideo(floatVideoView);

        finish();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.aty_float_video;
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
}
