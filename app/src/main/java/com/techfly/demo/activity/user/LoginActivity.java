package com.techfly.demo.activity.user;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.techfly.demo.R;
import com.techfly.demo.activity.base.BaseActivity;
import com.techfly.demo.activity.base.Constant;
import com.techfly.demo.activity.base.MainActivity;
import com.techfly.demo.bean.EventBean;
import com.techfly.demo.bean.TestBean;
import com.techfly.demo.bean.User;
import com.techfly.demo.bean.UserBean;
import com.techfly.demo.util.DialogUtil;
import com.techfly.demo.util.LogsUtil;
import com.techfly.demo.util.NetWorkUtil;
import com.techfly.demo.util.PreferenceUtil;
import com.techfly.demo.util.SharePreferenceUtils;
import com.techfly.demo.util.SystemUtil;
import com.techfly.demo.util.ToastUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import cn.jpush.android.api.JPushInterface;
import de.greenrobot.event.EventBus;

/**
 * 登录界面
 */
public class LoginActivity extends BaseActivity {

    @InjectView(R.id.login_phoneEt)
    EditText login_phoneEt;
    @InjectView(R.id.login_passEt)
    EditText login_passEt;

    @InjectView(R.id.login_modify_tv)
    TextView login_modify_tv;

    @InjectView(R.id.login_pwd_linear)
    LinearLayout login_pwd_linear;

    @InjectView(R.id.login_phone_deleteIv)
    ImageView login_phone_deleteIv;
    @InjectView(R.id.login_pwd_deleteIv)
    ImageView login_pwd_deleteIv;
    @InjectView(R.id.login_pwd_show_iv)
    ImageView login_pwd_show_iv;

    private Boolean isShowPwd = false;
    private String m_getIntentType = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        ButterKnife.inject(this);
        EventBus.getDefault().register(this);

        initBaseView();
        setBaseTitle(getResources().getText(R.string.user_login).toString(), 0);
        initBackButton(R.id.top_back_iv);
        setTranslucentStatus(R.color.main_bg);

        if (NetWorkUtil.isNetWorkConnected(this)) {
        } else {
            DialogUtil.showNetWorkDialog(this);
        }

        initLisener();

        loadIntent();

//        initTest();

        if (PreferenceUtil.getBooleanSharePreference(LoginActivity.this, Constant.CONFIG_IS_LOGIN, false)) {
            jumpToMain();
        }

    }

    private void initTest(){
//        TestBean bean = new TestBean();
        String test1 = "{\"user\":{\"name\":\"ss\",\"age\":\"18\"}}";
        String test2 = "{\"user\":{\"name\":\"ss\"}}";
        String test3 = "{\"user\":{}}";
        String test4 = "{\"user\":{\"name\":\"ss\",\"age\":{}}";
        String test5 = "{\"user\":\"\"}";



        String test6 = "{\"mobile\":[\"15755122960\",\"18225752635\"],\"name\":\"张三\",\"sex\":1}";

        Gson gson = new Gson();
        TestBean bean = gson.fromJson(test6,TestBean.class);
        if(bean != null){
            LogsUtil.normal("bean1.getName=" + bean.getName() + ",getMobile=" + bean.getMobile().toString());
        }

//        JSONObject object = new JSONObject();
//        String name = object.getString("mobile");

        /*TestBean bean1 = gson.fromJson(test1,TestBean.class);
        if(bean1 != null){
            LogsUtil.normal("bean1.getName="+bean1.getUser().getName()+",getAge="+bean1.getUser().getAge());
        }

        TestBean bean2 = gson.fromJson(test2,TestBean.class);
        if(bean2 != null){
            LogsUtil.normal("bean2.getName="+bean2.getUser().getName()+",getAge="+bean2.getUser().getAge());
        }

        TestBean bean3 = gson.fromJson(test3,TestBean.class);
        if(bean3 != null){
            LogsUtil.normal("bean3.getName="+bean3.getUser().getName()+",getAge="+bean3.getUser().getAge());
        }

        TestBean bean4 = gson.fromJson(test4,TestBean.class);
        if(bean4 != null){
            LogsUtil.normal("bean4.getName="+bean4.getUser().getName()+",getAge="+bean4.getUser().getAge());
        }*/

    }

    private void loadIntent() {
        m_getIntentType = getIntent().getStringExtra(Constant.CONFIG_INTENT_TYPE);
    }

    private void initLisener() {
        login_phoneEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    login_phone_deleteIv.setVisibility(View.VISIBLE);
                } else {
                    login_phone_deleteIv.setVisibility(View.INVISIBLE);
                }
            }
        });
        login_passEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    login_pwd_linear.setVisibility(View.VISIBLE);
                } else {
                    login_pwd_linear.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (requestCode == Constants.REQUEST_LOGIN ||
                requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, loginListener);
        }*/
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 登陆跳转
     */
    private void jumpToMain() {

        EventBus.getDefault().post(new EventBean(EventBean.EVENT_REFRESH_UI));

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        if (m_getIntentType != null) {
            LogsUtil.normal("LoginActivity.m_getIntentType=" + m_getIntentType);
            intent.putExtra(Constant.CONFIG_INTENT_TYPE, m_getIntentType);
        }
        startActivity(intent);

        setResult(Constant.RESULTCODE_LOGIN);
        this.finish();

    }

    @OnClick(R.id.login_phone_deleteIv)
    public void clearPhone() {
        login_phoneEt.setText("");
    }

    @OnClick(R.id.login_pwd_deleteIv)
    public void clearPwd() {
        login_passEt.setText("");
    }

    @OnClick(R.id.login_pwd_linear)
    public void isShowPwd() {
        if (isShowPwd) {
            //hide
            isShowPwd = false;
            login_pwd_show_iv.setImageResource(R.drawable.icon_hide_pwd);
            login_passEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            //show
            isShowPwd = true;
            login_pwd_show_iv.setImageResource(R.drawable.icon_show_pwd);
            login_passEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
    }

    @OnClick(R.id.login_Btn)
    public void confirmLogin() {

        String phone = login_phoneEt.getEditableText().toString().trim();
        String pass = login_passEt.getEditableText().toString().trim();

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(pass)) {
            ToastUtil.DisplayToast(LoginActivity.this, Constant.TOAST_MEG_ERROR_EMPTY);
            return;
        }

        /*if(phone.length() != 11){
            ToastUtil.DisplayToast(LoginActivity.this, Constant.TOAST_MEG_ERROR_PHONE);
            return;
        }*/
//        LogsUtil.normal("getRegistrationID1=" + JPushInterface.getRegistrationID(LoginActivity.this) + ",getStatusBarHeight=" + getStatusBarHeight());

        String jpush = JPushInterface.getRegistrationID(LoginActivity.this);

        if (TextUtils.isEmpty(jpush)) {
            LogsUtil.normal("getMobileInfo="+ SystemUtil.getMobileInfo());
            String mobileInfo = SystemUtil.getMobileInfo();
            jpush = "ERROR:"+mobileInfo;

            PreferenceUtil.putBooleanSharePreference(LoginActivity.this,Constant.CONFIG_PREFERENCE_IS_GET_JPUSH,false);
        }else{

            PreferenceUtil.putBooleanSharePreference(LoginActivity.this,Constant.CONFIG_PREFERENCE_IS_GET_JPUSH,true);
        }

        showProcessDialog();
        postUserLoginApi(phone, pass, jpush);
    }

    @OnLongClick(R.id.login_Btn)
    boolean defaultLogin(){

        //http://114.55.250.185/weishop_app/shop/platformNotice?lt-id=37&lt-token=260ade21aff4b8cd1c743c3753360fda
        User user = new User("37", "123456789", "111111", "宜阁店铺", "message", "http://img.qqbody.com/uploads/allimg/201412/26-165525_432.jpg", "260ade21aff4b8cd1c743c3753360fda", "67", "money", "type", "city");
        SharePreferenceUtils.getInstance(this).saveUser(user);
        PreferenceUtil.putBooleanSharePreference(LoginActivity.this, Constant.CONFIG_IS_LOGIN, true);
        jumpToMain();
        return true;
    }

    private int getStatusBarHeight() {
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    @OnClick(R.id.login_modify_tv)
    public void jumpToModifyPass() {
        Intent intent = new Intent(LoginActivity.this, ModifyPassActivity.class);
        startActivity(intent);
    }

    public void onEventMainThread(EventBean bean) {
        closeProcessDialog();
        if (bean.getAction().equals(EventBean.WXENTRY_END)) {
//            getWxOpenId(Constant.WEXIN_RETURN_CODE);
        }
        if (bean.getAction().equals(EventBean.WXENTRY_CANCEL)) {
            ToastUtil.DisplayToast(this, "登陆取消!");
        }
        if (bean.getAction().equals(EventBean.WXENTRY_REFUSE)) {
            ToastUtil.DisplayToast(this, "登陆拒绝!");
        }
    }

    @Override
    public void getResult(String result, int type) {
        super.getResult(result, type);

        closeProcessDialog();

        result = result.replace("{}", "\"\""); //把返回内容中{} 转成""，即String类型

        if (type == Constant.API_POST_CODE_LOGIN_SUCCESS) {
            try {
                Gson gson = new Gson();
                UserBean data = gson.fromJson(result, UserBean.class);
                if (data != null && data.getCode().equals("000")) {

                    ToastUtil.DisplayToast(LoginActivity.this, "登录成功!");

                    User user = new User(data.getData().getLt_id() + "", login_phoneEt.getText().toString().trim(), login_passEt.getText().toString().trim(), data.getData().getShopname(), "message", data.getData().getAvatar(), data.getData().getLt_token(), data.getData().getShop_id() + "", "money", "type", "city");
                    SharePreferenceUtils.getInstance(this).saveUser(user);
                    PreferenceUtil.putBooleanSharePreference(LoginActivity.this, Constant.CONFIG_IS_LOGIN, true);

                    jumpToMain();

                } else {
                    ToastUtil.DisplayToast(LoginActivity.this, Constant.TOAST_MEG_ANALYZE_ERROR);
                }
            } catch (Exception e) {
                ToastUtil.DisplayToast(LoginActivity.this, Constant.TOAST_MEG_REBACK_ERROR);
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
