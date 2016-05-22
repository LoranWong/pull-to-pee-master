package com.bao.ptp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    PullToRefreshView pullToRefreshView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SampleAdapter());

        pullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefreshView.setRefreshing(false);
                    }
                }, 3000);
            }
        });
    }


    private class SampleAdapter extends RecyclerView.Adapter<SampleHolder> {

        @Override
        public SampleHolder onCreateViewHolder(ViewGroup parent, int pos) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            return new SampleHolder(view);
        }

        @Override
        public void onBindViewHolder(SampleHolder holder, int pos) {
            holder.bindData(pos);
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    private class SampleHolder extends RecyclerView.ViewHolder {

        private final View mRootView;
        private final ImageView mImageViewIcon;

        public SampleHolder(View itemView) {
            super(itemView);

            mRootView = itemView;
            mImageViewIcon = (ImageView) itemView.findViewById(R.id.iv);
        }

        public void bindData(int pos) {
            //nothing
            mRootView.setBackgroundColor(Color.parseColor("#F5F5F5"));
            switch (pos){
                case 0:
                    mImageViewIcon.setImageResource(R.mipmap.image_0);
                    break;
                case 1:
                    mImageViewIcon.setImageResource(R.mipmap.image_1);
                    break;
                case 2:
                    mImageViewIcon.setImageResource(R.mipmap.image_2);
                    break;
                case 3:
                    mImageViewIcon.setImageResource(R.mipmap.image_3);
                    break;
                case 4:
                    mImageViewIcon.setImageResource(R.mipmap.image_4);
                    break;
                case 5:
                    mImageViewIcon.setImageResource(R.mipmap.image_5);
                    break;
            }
        }
    }


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        switch (event.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                mStart = event.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float diff = event.getY() - mStart;
//                float percent = (diff/mSlop)*100;
//                if(percent > 0 && percent <= 100){
//                    peeDrawable.setPercent(percent);
//                    imageView.invalidate();
//                }else if(percent > 100 && !peeDrawable.isRunning()){
//                    peeDrawable.start();
//                }
//
//                Log.i(PeeDrawable.TAG,"diff -> " + diff);
//                Log.i(PeeDrawable.TAG,"percent -> " + percent);
//
//                break;
//        }
//
//        return false;
//    }
}
