package com.zt.simplevideoplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_main);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.normal_video:
                startActivity(new Intent(this, NormalVideoActivity.class));
                break;
            case R.id.list_video:
                startActivity(new Intent(this, ListVideoActivity.class));
                break;
            case R.id.recyclerview_video:
                startActivity(new Intent(this, RecyclerViewVideoActivity.class));
                break;
        }
    }
}
