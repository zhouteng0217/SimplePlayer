package com.zt.listvideoplayer;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.zt.listvideoplayer.listvideoplayer.ListVideoPlayerStandard;
import com.zt.listvideoplayer.listvideoplayer.VideoPlayerManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public  static Activity mActivity;

    private ListView listView;
    private List<String> mDatas = new ArrayList<>();
    ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.list);
        initDatas();
        listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);

        mActivity = this;
    }

    private void initDatas() {
        mDatas.add("http://video.jiecao.fm/11/23/xin/%E5%81%87%E4%BA%BA.mp4");
        mDatas.add("http://video.jiecao.fm/8/17/bGQS3BQQWUYrlzP1K4Tg4Q__.mp4");
        mDatas.add("http://video.jiecao.fm/8/17/%E6%8A%AB%E8%90%A8.mp4");
        mDatas.add("http://video.jiecao.fm/8/18/%E5%A4%A7%E5%AD%A6.mp4");
        mDatas.add("http://video.jiecao.fm/8/16/%E8%B7%B3%E8%88%9E.mp4");
        mDatas.add("http://video.jiecao.fm/8/16/%E9%B8%AD%E5%AD%90.mp4");
        mDatas.add("http://video.jiecao.fm/8/16/%E9%A9%BC%E8%83%8C.mp4");
        mDatas.add("http://video.jiecao.fm/8/16/%E4%BF%AF%E5%8D%A7%E6%92%91.mp4");
    }

    public class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if(convertView == null) {
                convertView = View.inflate(MainActivity.this,R.layout.list_item_layout,null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.button.setText(position + "");
            viewHolder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoPlayerManager.getInstance().listVideoPlayer(listView,R.id.container,position,mDatas.get(position),position + "");
                }
            });
            return convertView;
        }

        class ViewHolder {

            public Button button;
            public RelativeLayout container;

            public ViewHolder(View convertView) {
                button = (Button) convertView.findViewById(R.id.test_button);
                container = (RelativeLayout) convertView.findViewById(R.id.container);
            }
        }

    }
}
