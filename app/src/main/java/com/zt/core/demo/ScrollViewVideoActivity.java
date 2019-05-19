package com.zt.core.demo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.zt.core.view.StandardVideoView;

public class ScrollViewVideoActivity extends NormalVideoActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.aty_scrollview_video;
    }

    @Override
    protected void setActionBarTitle() {
        getSupportActionBar().setTitle("ScrollView Video");
    }

    /**
     *  在scrollview，listview, 或recyclerview这种滚动视图中，播放视频，需要全屏操作时，为了防止全屏后，还可以滚动等操作，故采用了
     *
     *  重写下isFullScreenInScrollView返回true， 让其通过activity根布局添加和移除的方式来全屏，这种模式下，视频控件外层还需单独套一层布局，使其返回正常窗口状态时，不会顺序错乱
     *
     */
    public static class ScrollViewStandardVideoView extends StandardVideoView {

        public ScrollViewStandardVideoView(@NonNull Context context) {
            super(context);
        }

        public ScrollViewStandardVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public ScrollViewStandardVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected boolean isFullScreenInScrollView() {
            return true;
        }
    }
}
