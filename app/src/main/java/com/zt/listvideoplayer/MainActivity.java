package com.zt.listvideoplayer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.zt.listvideoplayer.listvideoplayer.VideoPlayerManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public  static Activity mActivity;

    private ListView listView;
    private List<String> mDatas = new ArrayList<>();
    private List<String> mThumbs = new ArrayList<>();
    ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.list);
        initImageLoader();
        initDatas();
        listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);

        mActivity = this;
    }

    private void initImageLoader() {
        ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .build();
        ImageLoader.getInstance().init(imageLoaderConfiguration);
    }

    private void initDatas() {
        mDatas.add("http://video.jiecao.fm/11/23/xin/%E5%81%87%E4%BA%BA.mp4");
        mThumbs.add("http://g.hiphotos.baidu.com/image/pic/item/b8389b504fc2d5620bbc0bfeed1190ef76c66c69.jpg");

        mDatas.add("http://video.jiecao.fm/8/17/bGQS3BQQWUYrlzP1K4Tg4Q__.mp4");
        mThumbs.add("http://b.hiphotos.baidu.com/image/pic/item/f2deb48f8c5494ee6980cb0a27f5e0fe99257e07.jpg");

        mDatas.add("http://video.jiecao.fm/8/17/%E6%8A%AB%E8%90%A8.mp4");
        mThumbs.add("http://d.hiphotos.baidu.com/image/pic/item/86d6277f9e2f07086386dc13e324b899a901f226.jpg");

        mDatas.add("http://video.jiecao.fm/8/18/%E5%A4%A7%E5%AD%A6.mp4");
        mThumbs.add("http://c.hiphotos.baidu.com/image/pic/item/7a899e510fb30f24e864edc9c195d143ac4b0379.jpg");

        mDatas.add("http://video.jiecao.fm/8/16/%E8%B7%B3%E8%88%9E.mp4");
        mThumbs.add("http://c.hiphotos.baidu.com/image/pic/item/50da81cb39dbb6fdb06f3f350324ab18972b37e9.jpg");

        mDatas.add("http://video.jiecao.fm/8/16/%E9%B8%AD%E5%AD%90.mp4");
        mThumbs.add("http://b.hiphotos.baidu.com/image/pic/item/3801213fb80e7bec1552032b262eb9389a506b6b.jpg");

        mDatas.add("http://video.jiecao.fm/8/16/%E9%A9%BC%E8%83%8C.mp4");
        mThumbs.add("http://g.hiphotos.baidu.com/image/pic/item/242dd42a2834349b15d26e4fc0ea15ce37d3be27.jpg");

        mDatas.add("http://video.jiecao.fm/8/16/%E4%BF%AF%E5%8D%A7%E6%92%91.mp4");
        mThumbs.add("http://d.hiphotos.baidu.com/image/pic/item/6c224f4a20a446234a817e719222720e0cf3d75e.jpg");

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
                    VideoPlayerManager.getInstance().listVideoPlayer(listView, R.id.container, position, mDatas.get(position), position + "", mThumbs.get(position), ImageLoader.getInstance());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoPlayerManager.getInstance().destory();
    }
}
