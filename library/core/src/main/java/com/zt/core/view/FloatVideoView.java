package com.zt.core.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.zt.core.R;
import com.zt.core.base.BaseVideoView;
import com.zt.core.base.ITinyVideoView;
import com.zt.core.util.VideoUtils;

/**
 * Created by zhouteng on 2019-09-13
 * <p>
 * 小窗口视频UI界面
 */
public class FloatVideoView extends BaseVideoView implements ITinyVideoView, View.OnClickListener, View.OnTouchListener {

    private ITinyVideoView.LayoutParams videoLayoutParams;
    private TinyVideoViewListenr listener;
    private ImageView playBtn;

    public FloatVideoView(@NonNull Context context) {
        this(context, null);
    }

    public FloatVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        videoLayoutParams = new ITinyVideoView.LayoutParams();
        videoLayoutParams.x = 0;
        videoLayoutParams.y = 0;
        videoLayoutParams.width = VideoUtils.dp2px(context, 200);
        videoLayoutParams.height = VideoUtils.dp2px(context, 112);

        View closeView = findViewById(R.id.close);
        closeView.setOnClickListener(this);

        View fullscreenView = findViewById(R.id.fullscreen);
        fullscreenView.setOnClickListener(this);

        playBtn = findViewById(R.id.start);
        playBtn.setOnClickListener(this);

        surfaceContainer.setOnTouchListener(this);

    }

    //禁止重力感应旋转
    @Override
    public boolean supportSensorRotate() {
        return false;
    }

    @Override
    protected int getSurfaceContainerId() {
        return R.id.surface_container;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.float_video_layout;
    }

    @Override
    public boolean onBackKeyPressed() {
        return false;
    }

    @Override
    public void setFloatVideoLayoutParams(ITinyVideoView.LayoutParams layoutParams) {
        if (layoutParams != null) {
            videoLayoutParams = layoutParams;
        }
    }

    @Override
    public ITinyVideoView.LayoutParams getFloatVideoLayoutParams() {
        return videoLayoutParams;
    }

    @Override
    public void setTinyVideoViewListener(TinyVideoViewListenr listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.close) {
            if (listener != null) {
                listener.closeVideoView();
            }
        } else if (viewId == R.id.fullscreen) {
            if (listener != null) {
                listener.backToNormalView();
            }
        } else if (viewId == R.id.start) {
            start();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (listener != null) {
            return listener.onTouch(event);
        }
        return false;
    }

    @Override
    protected void setPlayingIcon() {
        playBtn.setImageResource(R.drawable.ic_pause);
    }

    @Override
    protected void setPausedIcon() {
        playBtn.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void setTitle(String title) {

    }
}
