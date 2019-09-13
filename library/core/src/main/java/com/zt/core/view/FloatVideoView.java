package com.zt.core.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

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

    private VideoLayoutParams videoLayoutParams;
    private TinyVideoViewListenr listenr;

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
        videoLayoutParams = new VideoLayoutParams();
        videoLayoutParams.x = 0;
        videoLayoutParams.y = 0;
        videoLayoutParams.width = VideoUtils.dp2px(context, 200);
        videoLayoutParams.height = VideoUtils.dp2px(context, 112);

        View closeView = findViewById(R.id.close);
        closeView.setOnClickListener(this);

        View fullscreenView = findViewById(R.id.fullscreen);
        fullscreenView.setOnClickListener(this);

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
    public void setVideoLayoutParams(VideoLayoutParams layoutParams) {
        if (layoutParams != null) {
            videoLayoutParams = layoutParams;
        }
    }

    @Override
    public VideoLayoutParams getVideoLayoutParams() {
        return videoLayoutParams;
    }

    @Override
    public void setTinyVideoViewListener(TinyVideoViewListenr listener) {
        this.listenr = listener;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close) {
            if (listenr != null) {
                listenr.closeVideoView();
            }
        } else if (v.getId() == R.id.fullscreen) {
            if (listenr != null) {
                listenr.backToNormalView();
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (listenr != null) {
            return listenr.onTouch(event);
        }
        return false;
    }
}
