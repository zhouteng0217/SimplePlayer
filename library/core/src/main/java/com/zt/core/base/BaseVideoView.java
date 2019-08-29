package com.zt.core.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;

import com.zt.core.R;
import com.zt.core.listener.OnFullScreenChangedListener;
import com.zt.core.util.VideoUtils;

import java.util.Map;


public abstract class BaseVideoView extends FrameLayout {

    protected boolean isFullScreen = false;
    protected OnFullScreenChangedListener onFullScreenChangeListener;

    //正常状态下控件的宽高
    protected int originWidth;
    protected int originHeight;

    //父视图
    protected ViewParent viewParent;
    //当前view在父视图中的位置
    protected int positionInParent;

    protected int mSystemUiVisibility;

    protected AbstractVideoController videoController;
    protected ViewGroup surfaceContainer;

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
        surfaceContainer = findViewById(getSurfaceContainerId());
        videoController = new VideoController(surfaceContainer);
    }

    //region DataSource
    public void setVideoPath(String url) {
        videoController.setVideoPath(url);
    }

    public void setVideoPath(String url, Map<String, String> headers) {
        videoController.setVideoPath(url, headers);
    }

    //设置raw下视频的路径
    public void setVideoRawPath(@RawRes int rawId) {
        videoController.setVideoRawPath(rawId);
    }

    //设置assets下视频的路径
    public void setVideoAssetPath(String assetFileName) {
        videoController.setVideoAssetPath(assetFileName);
    }

    //endregion

    public void startVideo() {
        videoController.startVideo();
    }

    //region 全屏处理

    //视频全屏策略，竖向全屏，横向全屏，还是根据宽高比来选择
    protected int getFullScreenOrientation() {
       return videoController.getFullScreenOrientation();
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void setOnFullScreenChangeListener(OnFullScreenChangedListener onFullScreenChangeListener) {
        this.onFullScreenChangeListener = onFullScreenChangeListener;
    }

    /**
     * 表示是否要在滚动控件(scrollview,listview ,recyclerview) 里面播放视频来全屏操作
     *
     * @return
     */
    protected boolean isFullScreenInScrollView() {
        return true;
    }

    protected void startFullScreen() {

        isFullScreen = true;

        Activity activity = VideoUtils.getActivity(getContext());

        mSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();

        activity.setRequestedOrientation(getFullScreenOrientation());

        VideoUtils.hideSupportActionBar(activity, true);
        VideoUtils.addFullScreenFlag(activity);
        VideoUtils.hideNavKey(activity);

        if (isFullScreenInScrollView()) {
            changeToFullScreenInScrollView();
        } else {
            changeToFullScreen();
        }

        postRunnableToResizeTexture();

        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullScreenChange(true);
        }
    }

    //正常全屏操作
    protected void changeToFullScreen() {
        originWidth = getWidth();
        originHeight = getHeight();

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        setLayoutParams(layoutParams);
    }

    /**
     * 通过获取到Activity的ID_ANDROID_CONTENT根布局，来添加视频控件，并全屏
     * <p>
     * 这种模式，为了全屏后，能顺利回到原来的位置，需要在布局时，单独给视频控件添加一层父控件，
     * <p>
     * 用于滚动视图，列表视图播放器全屏
     */
    protected void changeToFullScreenInScrollView() {

        originWidth = getWidth();
        originHeight = getHeight();

        viewParent = getParent();
        positionInParent = ((ViewGroup)viewParent).indexOfChild(this);

        ViewGroup vp = getRootViewGroup();

        removePlayerFromParent();

        LayoutParams lpParent = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setBackgroundColor(Color.BLACK);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayout.addView(this, lp);
        vp.addView(frameLayout, lpParent);
    }

    /**
     * 获取到Activity的根布局
     * @return
     */
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

        if (isFullScreenInScrollView()) {
            changeToNormalScreenInScrollView();
        } else {
            changeToNormalScreen();
        }

        postRunnableToResizeTexture();

        if (onFullScreenChangeListener != null) {
            onFullScreenChangeListener.onFullScreenChange(false);
        }
    }

    //正常的回到全屏前状态
    protected void changeToNormalScreen() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = originWidth;
        layoutParams.height = originHeight;
        setLayoutParams(layoutParams);
    }

    /**
     * 对应上面的全屏模式，来恢复到全屏之前的样式，需要视频控件外出套了一层父控件，以方便添加回去
     */
    protected void changeToNormalScreenInScrollView() {
        ViewGroup vp = getRootViewGroup();
        vp.removeView((View) this.getParent());
        removePlayerFromParent();

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(originWidth, originHeight);
        setLayoutParams(layoutParams);

        if (viewParent != null) {
            ((ViewGroup) viewParent).addView(this, positionInParent);
        }
    }


    //endregion

    //region 播放控制

    protected boolean isPlaying() {
        return videoController.isPlaying();
    }

    public void start() {
        videoController.start();
    }

    public void release() {
        videoController.release();
    }

    protected void replay() {
        videoController.replay();
    }

    public void destroy() {
        videoController.destroy();
    }

    public void pause() {
        videoController.pause();
    }
    //endregion

    protected void postRunnableToResizeTexture() {
        post(new Runnable() {
            @Override
            public void run() {
                videoController.resizeTextureView(videoController.getVideoWidth(), videoController.getVideoHeight());
            }
        });
    }

    protected abstract @IdRes int getSurfaceContainerId();

    protected abstract int getLayoutId();

    public abstract boolean onBackKeyPressed();

    public abstract void setTitle(String titleText);

    public abstract void changeUIWithState(int state);

    public void setPlayerConfig(PlayerConfig playerConfig) {
        videoController.setPlayerConfig(playerConfig);
    }

    /**
     * 默认的VideoController实现，定义了默认的数据连接情况下，弹出提示框
     */
    class VideoController extends AbstractVideoController {

        private boolean isShowMobileDataDialog = false;

        private VideoController(ViewGroup surfaceContainer) {
            super(surfaceContainer);
        }

        @Override
        protected void showMobileDataDialog() {
            if (isShowMobileDataDialog) {
                return;
            }
            isShowMobileDataDialog = true;

            AlertDialog.Builder builder = new AlertDialog.Builder(surfaceContainer.getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
            builder.setMessage(surfaceContainer.getResources().getString(R.string.mobile_data_tips));
            builder.setPositiveButton(surfaceContainer.getResources().getString(R.string.continue_playing), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startVideo();
                }
            });
            builder.setNegativeButton(surfaceContainer.getResources().getString(R.string.stop_play), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }

        @Override
        public void onStateChange(int state) {
            changeUIWithState(state);
        }
    }
}
