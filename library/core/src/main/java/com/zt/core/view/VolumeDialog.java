package com.zt.core.view;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.zt.core.R;
import com.zt.core.base.BaseDialog;
import com.zt.core.util.VideoUtils;

public class VolumeDialog extends BaseDialog {

    protected ProgressBar volumeProgressBar;

    public VolumeDialog(@NonNull Context context) {
        super(context);
    }

    public VolumeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_volume;
    }

    @Override
    protected void initView(View view) {
        volumeProgressBar = view.findViewById(R.id.volumn_progress_bar);
    }

    public void showVolumeDialog(int volumeProgress, View anchorView) {

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP | Gravity.START;

        layoutParams.width = VideoUtils.dp2px(getContext(), 200);
        layoutParams.height = VideoUtils.dp2px(getContext(), 100);

        int videoWidth = anchorView.getWidth();
        int videoHeight = anchorView.getHeight();

        int statusBarHeight = VideoUtils.dp2px(getContext(), 25);

        int[] location = getViewLocation(anchorView);

        layoutParams.x = location[0] + videoWidth / 2 - layoutParams.width / 2;
        layoutParams.y = location[1] + videoHeight / 2 - layoutParams.height / 2 - statusBarHeight;
        getWindow().setAttributes(layoutParams);

        volumeProgressBar.setProgress(volumeProgress);

        if (!isShowing()) {
            show();
        }
    }

}
