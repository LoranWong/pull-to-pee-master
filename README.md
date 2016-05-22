# pull-to-pee-master
A funny Pull to refresh view on Android , you can add your own RecyclerView
一个有趣的下拉刷新 (我也不知道我为什么就要做这个下拉刷新动画)

![image](https://github.com/LoranWong/pull-to-pee-master/blob/master/demo.gif)


### Usage

#### 1.First , copy PeeDrawable and PullToRefreshLayout to your project

#### 2.In XML  Directly

        <com.bao.ptp.PullToRefreshView
            android:id="@+id/pull_to_refresh"
            android:layout_below="@+id/app_bar"
            android:layout_width="match_parent"
            android:layout_height="match_parent">

            <android.support.v7.widget.RecyclerView
                android:id="@+id/recycler_view"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:divider="#ffffff"
                android:dividerHeight="1dp"
                android:fadingEdge="vertical"
                android:fadingEdgeLength="5dp"/>

        </com.bao.ptp.PullToRefreshView>
    
#### 3. In Java Code

        pullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SampleAdapter());

        pullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // your code
            }
        });


#### Done :D
