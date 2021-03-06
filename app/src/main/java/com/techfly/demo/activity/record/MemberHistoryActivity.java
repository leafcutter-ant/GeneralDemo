package com.techfly.demo.activity.record;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.techfly.demo.R;
import com.techfly.demo.activity.base.BaseActivity;
import com.techfly.demo.activity.base.Constant;
import com.techfly.demo.adpter.MemberHistoryAdapter;
import com.techfly.demo.bean.MemberHistoryBean;
import com.techfly.demo.bean.User;
import com.techfly.demo.interfaces.ItemClickListener;
import com.techfly.demo.util.CommonUtils;
import com.techfly.demo.util.SharePreferenceUtils;
import com.techfly.demo.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/*
 * 会员记录
 */
public class MemberHistoryActivity extends BaseActivity {

    @InjectView(R.id.member_numbers_tv)
    TextView member_numbers_tv;
    @InjectView(R.id.base_plv)
    PullToRefreshListView base_plv;

    private User mUser;
    private int mPage = 1;
    private int mSize = 10;
    private int mTotalRecord = 0;
    private boolean isRefresh = false;

    private MemberHistoryAdapter adapter = null;
    private List<MemberHistoryBean.DataEntity.DatasEntity> datasEntityList = new ArrayList<MemberHistoryBean.DataEntity.DatasEntity>();

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_member_history);

        ButterKnife.inject(this);

        initBaseView();
        setBaseTitle("会员记录", 0);
        initBackButton(R.id.top_back_iv);
        setTranslucentStatus(R.color.main_bg);

        initView();

        initLisener();

        initAdapter();

        refresh();
    }

    private void initView() {
        mUser = SharePreferenceUtils.getInstance(this).getUser();
    }

    private void initLisener() {
        base_plv.setMode(PullToRefreshBase.Mode.BOTH);
        base_plv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                refresh();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadMore();
            }
        });
    }

    private void initAdapter() {
        adapter = new MemberHistoryAdapter(MemberHistoryActivity.this, datasEntityList);
        base_plv.setAdapter(adapter);

        adapter.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
            }

            @Override
            public void onItemLongClick(View view, int postion) {
            }

            @Override
            public void onItemSubViewClick(View view, int postion) {

            }
        });
    }

    private void loadMore() {
        if (mUser != null) {
            if (adapter.getCount() < mTotalRecord) {
                mPage = mPage + 1;
                getMemberHistoryListApi(mUser.getmId(), mUser.getmToken(), mPage, mSize, mUser.getiCode());
            } else {
                ToastUtil.DisplayToast(MemberHistoryActivity.this, Constant.TOAST_MEG_LOADING_FINISH);
                base_plv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        base_plv.onRefreshComplete();
                    }
                }, 200);
            }
        }
    }

    private void refresh() {
        if (mUser != null) {
            mPage = 1;
            mSize = 10;
            //清除之前的数据，加载最新的数据
            adapter.clearAll();
            getMemberHistoryListApi(mUser.getmId(), mUser.getmToken(), mPage, mSize, mUser.getiCode());
        }
    }

    @Override
    public void getResult(String result, int type) {
        super.getResult(result, type);

        result = CommonUtils.removeBrace(result);
        base_plv.onRefreshComplete();

        if (type == Constant.API_ANALYZE_SUCCESS) {
            try {
                Gson gson = new Gson();
                MemberHistoryBean data = gson.fromJson(result, MemberHistoryBean.class);
                if (data != null) {

                    mTotalRecord = data.getData().getTotalRecord();
                    member_numbers_tv.setText("会员人数:"+mTotalRecord);

                    adapter.addAll(data.getData().getDatas());
                } else {
                    ToastUtil.DisplayToast(MemberHistoryActivity.this, Constant.TOAST_MEG_ANALYZE_ERROR);
                }
            } catch (Exception e) {
                ToastUtil.DisplayToast(MemberHistoryActivity.this, Constant.TOAST_MEG_REBACK_ERROR);
                e.printStackTrace();
            }
        }
    }


}
