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

import com.zt.core.base.BasePlayer;
import com.zt.core.base.IFloatView;
import com.zt.core.base.RenderContainerView;
import com.zt.core.player.FloatVideoManager;
import com.zt.core.view.FloatVideoView;
import com.zt.core.view.StandardVideoView;

/**
 * Created by zhouteng on 2019-09-14
 * <p>
 * 用于展示小窗口视频播放实例demo
 */
public class FloatVideoActivity extends BaseDemoActivity implements CompoundButton.OnCheckedChangeListener {

    public static final int REQUEST_DRAWOVERLAYS_CODE = 10000;

    private StandardVideoView videoView;
    private CheckBox checkBox;

    //正在播放的小窗口视频画面层
    private RenderContainerView floatRender;

    @Override
    protected void initView() {

        FrameLayout container = findViewById(R.id.video_view_container);

        checkBox = findViewById(R.id.checkbox);
        initCheckBox();

        floatRender = FloatVideoManager.getInstance().getRenderContainerViewOffParent();

        videoView = new StandardVideoView(this) {
            @Override
            protected RenderContainerView newRenderContainerView() {

                //将悬浮小窗口中视频画面取出来，放置
                if (floatRender != null) {
                    return floatRender;
                }

                //否则，就是没有小窗口，新建一个
                //由于后面要将RenderContainerView层添加到window层，为防止内存泄露，重写了newRenderContainerView, 用application的context来构建
                return new RenderContainerView(getApplicationContext());
            }
        };

        container.addView(videoView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        initPlayerView(videoView);

        initDescView(findViewById(R.id.desc));

        if (floatRender != null) {
            //有正常播放的小窗口时
            videoView.setPlayerStatus(FloatVideoManager.getInstance().getCurrentPlayState());
            FloatVideoManager.getInstance().destroyVideoView();

        } else {
            videoView.start();
        }
    }

    @Override
    protected BasePlayer getPlayer() {

        //有当前正在播放的小窗口视频时，获取到当前的播放器实例
        if (floatRender != null) {
            return floatRender.getPlayer();
        }

        //没有正在播放的小窗口
        return super.getPlayer();
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

        if (checkBox.isChecked()) {
            //只销毁播放器UI控制层上面相关要销毁的要素，不销毁播放器和渲染界面实例，用于添加到window层
            videoView.destroyPlayerController();
            startFloatVideoView();
        } else {
            videoView.destroy();
        }
    }

    private void startFloatVideoView() {
        //注意使用application的context构建，防止内存泄露
        FloatVideoView floatVideoView = new FloatVideoView(getApplicationContext());

        IFloatView.LayoutParams layoutParams = new IFloatView.LayoutParams(600, 336);
        layoutParams.x = 20;
        layoutParams.y = 20;
        floatVideoView.setFloatVideoLayoutParams(layoutParams);

        //将正在播放的VideoView的RenderContainer层从原来界面剥离出来，添加到自定义的悬浮窗VideoView上
        RenderContainerView renderContainerView = videoView.getRenderContainerViewOffParent();

        floatVideoView.addRenderContainer(renderContainerView);

        floatVideoView.setPlayerStatus(videoView.getCurrentState());

        //构建跳回来的intent, 为了防止内存泄漏，注意使用application的context，加上flags
        Intent intent = new Intent(getApplicationContext(), FloatVideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("sample", sample);

        FloatVideoManager.getInstance().startFloatVideo(floatVideoView, intent);

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
