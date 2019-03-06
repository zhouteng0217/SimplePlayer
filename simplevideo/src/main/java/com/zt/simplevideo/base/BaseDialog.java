package com.zt.simplevideo.base;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

public abstract class BaseDialog extends Dialog {


    public BaseDialog(@NonNull Context context) {
        super(context);
        init(context);
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected void init(Context context) {
        View view = LayoutInflater.from(context).inflate(getLayoutId(), null);
        setContentView(view);

        getWindow().addFlags(Window.FEATURE_ACTION_BAR);

        initView(view);
    }

    protected abstract @LayoutRes int getLayoutId();

    protected abstract void initView(View view);

    protected int[] getViewLocation(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location;
    }
}
