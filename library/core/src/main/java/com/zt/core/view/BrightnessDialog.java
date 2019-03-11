package com.zt.core.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.zt.core.R;
import com.zt.core.base.BaseDialog;
import com.zt.core.util.VideoUtils;

public class BrightnessDialog extends BaseDialog {

    private ProgressBar brightnessProgressBar;

    public BrightnessDialog(@NonNull Context context) {
        super(context);
    }

    public BrightnessDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_brightness;
    }

    @Override
    protected void initView(View view) {
        brightnessProgressBar = view.findViewById(R.id.brightness_progress_bar);
    }

    public void showBrightnewssDialog(int brightnessProgress, View anchorView) {

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

        brightnessProgressBar.setProgress(brightnessProgress);

        if (!isShowing()) {
            show();
        }
    }

}
