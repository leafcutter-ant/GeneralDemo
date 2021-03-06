package com.techfly.demo.activity.refund;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.techfly.demo.R;
import com.techfly.demo.activity.base.Constant;
import com.techfly.demo.adpter.ShopOrderRefundLvAdapter;
import com.techfly.demo.bean.EventBean;
import com.techfly.demo.bean.ReasultBean;
import com.techfly.demo.bean.ShopRefundOrderListBean;
import com.techfly.demo.bean.User;
import com.techfly.demo.fragment.BaseFragment;
import com.techfly.demo.interfaces.ItemMoreClickListener;
import com.techfly.demo.util.CommonUtils;
import com.techfly.demo.util.DialogUtil;
import com.techfly.demo.util.LogsUtil;
import com.techfly.demo.util.SharePreferenceUtils;
import com.techfly.demo.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/*
 * 退货退款
 */
public class OBFrag extends BaseFragment {

    @InjectView(R.id.base_plv)
    PullToRefreshListView base_plv;
    @InjectView(R.id.base_load)
    View base_load;

    private Context mContext;
    private View view;

    private User mUser = null;
    private int mPage = 1;
    private int mSize = 10;

    private String mStatus = "RECEIVE_WAITTING,FINISHED&refund_status=REFUND_APPLY";
    private int mStatusCode = 3;

    private int mTotalRecord = 0;

    private Boolean isVisible = false;
    private Boolean isInit = false;

    private ShopOrderRefundLvAdapter adapter;
    private List<ShopRefundOrderListBean.DataEntity.DatasEntity> datasEntities = new ArrayList<ShopRefundOrderListBean.DataEntity.DatasEntity>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_base_pulllistview, container, false);
        mContext = getActivity();

        ButterKnife.inject(this, view);
        EventBus.getDefault().register(this);

        initView();

        initAdapter();

        initLisener();

        if (isVisible) {
            refresh();
        }
        isInit = true;

        return view;
    }

    private void initView() {
        mUser = SharePreferenceUtils.getInstance(getActivity()).getUser();
    }

    private void initAdapter() {

        adapter = new ShopOrderRefundLvAdapter(mContext, datasEntities, mStatusCode);
        base_plv.setAdapter(adapter);

        adapter.setItemClickListener(new ItemMoreClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                ShopRefundOrderListBean.DataEntity.DatasEntity bean = (ShopRefundOrderListBean.DataEntity.DatasEntity) adapter.getItem(postion);

                LogsUtil.normal("postion=" + postion + ",getOrder_id=" + bean.getOrder_id());

                Intent intent = new Intent(mContext,RefundOrderDetailActivity.class);
                intent.putExtra(Constant.CONFIG_INTENT_ORDER_ID,bean.getOrder_id()+"");
                intent.putExtra(Constant.CONFIG_INTENT_TYPE,"2");
                startActivityForResult(intent, Constant.REQUESTCODE_NORMAL);
            }

            @Override
            public void onItemSubViewClick(int choice, int postion) {
            }

            @Override
            public void onItemMulViewClick(int type, int choice, int postion) {

                ShopRefundOrderListBean.DataEntity.DatasEntity bean = (ShopRefundOrderListBean.DataEntity.DatasEntity) adapter.getItem(postion);

                if (type == mStatusCode) {
                    switch (choice) {
                        case 0:
                            showRefundDialog(mContext,bean.getOrder_id()+"");
//                            DialogUtil.showDeleteDialog(mContext, "温馨提示", "您确认同意退款?", EventBean.CONFIRM_ORDER_CONFIRM_REFUND, bean.getOrder_id() + "");
                            break;
                        case 1:
                            DialogUtil.showDeleteDialog(mContext, "温馨提示", "您确认拒绝退货退款退款?", EventBean.CONFIRM_ORDER_REFUSE_REFUND,bean.getOrder_id() + "");
                            break;
                        case 2:
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + bean.getMobile()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            break;
                    }
                }
            }
        });

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

    private void loadMore() {
        if (adapter.getCount() < mTotalRecord) {
            mPage = mPage + 1;
            getShopRefundOrderListApi(mUser.getmId(), mUser.getmToken(), mPage, mSize,mUser.getiCode(),mStatus);
        } else {
            ToastUtil.DisplayToast(mContext, Constant.TOAST_MEG_LOADING_FINISH);
            base_plv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    base_plv.onRefreshComplete();
                }
            }, 200);
        }
    }

    private void refresh() {
        mPage = 1;
        mSize = 10;

        //清除之前的数据，加载最新的数据
        adapter.clearAll();
        if (mUser != null) {
            getShopRefundOrderListApi(mUser.getmId(), mUser.getmToken(), mPage, mSize,mUser.getiCode(),mStatus);
        } else {
            ToastUtil.DisplayToast(mContext, Constant.TOAST_MEG_LOGIN_FIRST);
        }
    }


    public void showRefundDialog(final Context context,final String oId) {

        final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
        dialog.show();

        //解决EditText不弹出输入框的问题
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);  //此处可以设置dialog显示的位置
//        window.setWindowAnimations(R.style.toastdig);  //添加动画
        View view = View.inflate(mContext, R.layout.dialog_submit_info, null);
        window.setContentView(view);

        final EditText moneyEt = (EditText) view.findViewById(R.id.submit_money_et);
        TextView tv3 = (TextView) view.findViewById(R.id.submit_confirm_tv);

        tv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
                String m_money = moneyEt.getEditableText().toString();

                if (TextUtils.isEmpty(m_money)) {
                    ToastUtil.DisplayToast(context, "退款金额不能为空!");
                    return;
                }
                postReturnRefundStatusApi(mUser.getmId(), mUser.getmToken(), oId , "REFUNDED", m_money);

                dialog.dismiss();
            }
        });

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            isVisible = true;
            if (isInit) {
                refresh();
            }
        } else {
            isVisible = false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUESTCODE_NORMAL && resultCode == getActivity().RESULT_OK) {
            refresh();
        }
    }

    @Override
    public void getResult(String result, int type) {
        super.getResult(result, type);

//        LogsUtil.normal("getResult1");

        base_plv.onRefreshComplete();
        base_load.setVisibility(View.INVISIBLE);
        Gson gson = new Gson();

//        result = result.replace("{}", "\"\"");
        result = CommonUtils.removeBrace(result);

        try {
            if (type == Constant.API_GET_SHOP_REFUND_LIST_SUCCESS) {
                ShopRefundOrderListBean data = gson.fromJson(result, ShopRefundOrderListBean.class);
                if (data != null) {
                    mTotalRecord = data.getData().getTotalRecord();
                    adapter.addAll(data.getData().getDatas(), mStatusCode);

                    if (mTotalRecord == 0) {
                        ToastUtil.DisplayToast(mContext, Constant.TOAST_MEG_REBACK_EMPTY);
                    }
                } else {
                    ToastUtil.DisplayToast(mContext, Constant.TOAST_MEG_ANALYZE_ERROR);
                }
            }

            if (type == Constant.API_POST_SHOP_RETURN_REFUND_SUCCESS) {
                ReasultBean data = gson.fromJson(result, ReasultBean.class);
                if (data != null) {
                    DialogUtil.showSuccessDialog(mContext, data.getData(), EventBean.EVENT_EMPTY);
                    refresh();
                } else {
                    ToastUtil.DisplayToast(mContext, Constant.TOAST_MEG_ANALYZE_ERROR);
                }
            }

            if (type == Constant.API_POST_SHOP_REFUND_STATUS_SUCCESS) {
                ReasultBean data = gson.fromJson(result, ReasultBean.class);
                if (data != null) {
                    DialogUtil.showSuccessDialog(mContext, data.getData(), EventBean.EVENT_EMPTY);
                    refresh();
                } else {
                    ToastUtil.DisplayToast(mContext, Constant.TOAST_MEG_ANALYZE_ERROR);
                }
            }

        } catch (JsonSyntaxException e) {
            ToastUtil.DisplayToastDebug(mContext, Constant.TOAST_MEG_REBACK_ERROR + "\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onEventMainThread(EventBean bean) {

        if (bean.getAction().equals(EventBean.CONFIRM_ORDER_CONFIRM_REFUND) && isVisible) {
            String oId = bean.getMsg();
        }

        if (bean.getAction().equals(EventBean.CONFIRM_ORDER_REFUSE_REFUND) && isVisible) {
            String oId = bean.getMsg();
            postReturnRefundStatusApi(mUser.getmId(), mUser.getmToken(), oId, "REFUND_DENY", "");

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
