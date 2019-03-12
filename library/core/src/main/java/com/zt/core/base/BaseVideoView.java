package com.zt.core.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;

import com.zt.core.R;
import com.zt.core.listener.OnFullScreenChangedListener;
import com.zt.core.listener.OnStateChangedListener;
import com.zt.core.listener.PlayerListener;
import com.zt.core.player.AndroidPlayer;
import com.zt.core.render.SurfaceRenderView;
import com.zt.core.render.TextureRenderView;
import com.zt.core.util.VideoUtils;

import java.util.Map;


public abstract class BaseVideoView extends FrameLayout implements OnStateChangedListener, PlayerListener {

    protected BasePlayer player;

    private BaseRenderView renderView;

    protected boolean isFullScreen = false;
    protected OnFullScreenChangedListener onFullScreenChangeListener;

    //正常状态下控件的宽高
    protected int originWidth;
    protected int originHeight;

    protected ViewParent viewParent;

    protected int mSystemUiVisibility;

    protected boolean isShowMobileDataDialog = false;

    private PlayerConfig playerConfig;

    private String url;
    private Map<String, String> headers;

    public BaseVideoView(@NonNull Context context) {
        this(context, null);
    }

    public BaseVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context) {
        LayoutInflater.from(context).inflate(getLayoutId(), this);
        playerConfig = new PlayerConfig.Builder().build();
    }

    public void setVideoPath(String url) {
        setVideoPath(url, null);
    }

    public void setVideoPath(String url, Map<String, String> headers) {
        this.url = url;
        this.headers = headers;
    }

    public void startVideo() {
        int currentState = player == null ? BasePlayer.STATE_IDLE : player.getCurrentState();
        if (currentState == BasePlayer.STATE_IDLE || currentState == BasePlayer.STATE_ERROR) {
            prepareToPlay();
        } else if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    private void initPlayer() {
        player = newPlayerInstance(getContext());
        player.setOnStateChangeListener(this);
        player.setPlayerListener(this);
        player.setPlayerConfig(playerConfig);
        player.setVideoPath(url, headers);
        player.initPlayer();
    }

    protected void prepareToPlay() {

        initPlayer();

        ViewGroup surfaceContainer = getSurfaceContainer();
        surfaceContainer.removeAllViews();

        LayoutParams layoutParams =
                new LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);

        renderView = newRenderViewInstance(getContext());
        if (renderView != null) {
            renderView.setPlayer(player);
            surfaceContainer.addView(renderView.getRenderView(), layoutParams);
        }
    }

    //region 全屏处理

    //视频全屏策略，竖向全屏，横向全屏，还是根据宽高比来选择
    protected int getFullScreenOrientation() {
        if (playerConfig.screenMode == PlayerConfig.PORTRAIT_FULLSCREEN_MODE) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        if (playerConfig.screenMode == PlayerConfig.AUTO_FULLSCREEN_MODE) {
            return player.getAspectRation() >= 1 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void setOnFullScreenChangeListener(OnFullScreenChangedListener onFullScreenChangeListener) {
        this.onFullScreenChangeListener = onFullScreenChangeListener;
    }

    protected void startFullScreen() {

        isFullScreen = true;

        Activity activity = VideoUtils.getActivity(getContext());

        mSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();

        activity.setRequestedOrientation(getFullScreenOrientation());

        VideoUtils.hideSupportActionBar(activity, true);
        VideoUtils.addFullScreenFlag(activity);
        VideoUtils.hideNavKey(activity);

        changeToFullScreen();

        postRunnableToResizeTexture();

        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullScreenChange(true);
        }
    }

    protected void changeToFullScreen() {

        originWidth = getWidth();
        originHeight = getHeight();

        viewParent = getParent();

        ViewGroup vp = getRootViewGroup();

        removePlayerFromParent();

        final LayoutParams lpParent = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setBackgroundColor(Color.BLACK);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayout.addView(this, lp);
        vp.addView(frameLayout, lpParent);
    }

    protected ViewGroup getRootViewGroup() {
        Activity activity = (Activity) getContext();
        if (activity != null) {
            return (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        }
        return null;
    }

    protected void removePlayerFromParent() {
        ViewParent parent = getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }
    }

    protected void exitFullscreen() {

        isFullScreen = false;

        Activity activity = VideoUtils.getActivity(getContext());

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        VideoUtils.showSupportActionBar(activity, true);   //根据需要是否显示actionbar和状态栏
        VideoUtils.clearFullScreenFlag(activity);

        activity.getWindow().getDecorView().setSystemUiVisibility(mSystemUiVisibility);

        changeToNormalScreen();

        postRunnableToResizeTexture();

        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullScreenChange(false);
        }
    }

    protected void changeToNormalScreen() {
        ViewGroup vp = getRootViewGroup();
        vp.removeView((View) this.getParent());
        removePlayerFromParent();

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(originWidth, originHeight);
        setLayoutParams(layoutParams);

        if (viewParent != null) {
            ((ViewGroup) viewParent).addView(this);
        }
    }


    //endregion

    //region 播放控制

    protected boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public void start() {
        if (!TextUtils.isEmpty(url) && !url.startsWith("file") && !VideoUtils.isWifiConnected(getContext()) && !isShowMobileDataDialog) {
            showMobileDataDialog();
            return;
        }
        startVideo();
    }

    public void showMobileDataDialog() {
        if (isShowMobileDataDialog) {
            return;
        }
        isShowMobileDataDialog = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage(getResources().getString(R.string.mobile_data_tips));
        builder.setPositiveButton(getResources().getString(R.string.continue_playing), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startVideo();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.stop_play), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void release() {
        if (player != null) {
            player.release();
        }
    }

    protected void replay() {
        release();
        start();
    }

    public void destroy() {
        if (player != null) {
            player.destroy();
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }
    //endregion

    protected void postRunnableToResizeTexture() {
        post(new Runnable() {
            @Override
            public void run() {
                resizeTextureView(player.getVideoWidth(), player.getVideoHeight());
            }
        });
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        resizeTextureView(width, height);
    }

    //根据视频内容重新调整视频渲染区域大小
    protected void resizeTextureView(int width, int height) {
        if (width == 0 || height == 0 || renderView == null || renderView.getRenderView() == null) {
            return;
        }
        float aspectRation = (float) width / height;

        View surfaceContainer = getSurfaceContainer();
        int parentWidth = surfaceContainer.getWidth();
        int parentHeight = surfaceContainer.getHeight();

        int w, h;

        if (aspectRation >= 1) {
            w = parentWidth;
            h = (int) (w / aspectRation);
        } else {
            h = parentHeight;
            w = (int) (h * aspectRation);
        }

        ViewGroup.LayoutParams layoutParams = renderView.getRenderView().getLayoutParams();
        layoutParams.width = w;
        layoutParams.height = h;
        renderView.getRenderView().setLayoutParams(layoutParams);
    }

    //方便扩展播放器核心
    protected BasePlayer newPlayerInstance(Context context) {
        if (playerConfig != null && playerConfig.player != null) {
            return playerConfig.player;
        }
        return new AndroidPlayer(context);
    }

    //方便扩展播放器渲染界面
    protected BaseRenderView newRenderViewInstance(Context context) {
        switch (playerConfig.renderType) {
            case PlayerConfig.RENDER_TEXTURE_VIEW:
                return new TextureRenderView(context);
            case PlayerConfig.RENDER_SURFACE_VIEW:
                return new SurfaceRenderView(context);
        }
        return null;
    }

    protected abstract ViewGroup getSurfaceContainer();

    protected abstract int getLayoutId();

    public abstract boolean onBackKeyPressed();

    public abstract void setTitle(String titleText);

    @Override
    public abstract void onStateChange(int state);

    public void setPlayerConfig(PlayerConfig playerConfig) {
        this.playerConfig = playerConfig;
    }
}
