package com.lemonxq_laplace.pregnantmonitor.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lemonxq_laplace.pregnantmonitor.Data.User;
import com.lemonxq_laplace.pregnantmonitor.R;
import com.lemonxq_laplace.pregnantmonitor.Util.Consts;
import com.lemonxq_laplace.pregnantmonitor.Util.Server;
import com.lemonxq_laplace.pregnantmonitor.Util.SharedPreferencesUtil;
import com.lemonxq_laplace.pregnantmonitor.Util.UserManager;
import com.lemonxq_laplace.pregnantmonitor.Util.Util;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;


public class LogInActivity extends BaseActivity {

    private ProgressDialog progressDialog;
    private Button loginBtn;
    private Button registerBtn;
    private TextView visitorText;
    private EditText accountText;
    private EditText passwordText;
    private CheckBox isRememberPwd;
    private CheckBox isAutoLogin;

    private String account;
    private String password;

    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponents();
        setListeners();

        // 自动填充
        SharedPreferencesUtil spu = new SharedPreferencesUtil(this);
        Boolean isRemember = (Boolean) spu.getParam("isRememberPwd",false);
        Boolean isAutoLogin = (Boolean) spu.getParam("isAutoLogin",false);
        // SharedPreference获取用户账号密码，存在则填充
        String account = (String) spu.getParam("account","");
        String pwd = (String)spu.getParam("pwd","");
        if(!account.equals("") && !pwd.equals("")){
            if(isRemember){
                accountText.setText(account);
                passwordText.setText(pwd);
                isRememberPwd.setChecked(true);
            }
            if(isAutoLogin)
                Login();
        }
    }

    void initComponents(){
        server = new Server(this);
        loginBtn = findViewById(R.id.login);
        registerBtn = findViewById(R.id.register);
        visitorText = findViewById(R.id.visitor);
        accountText = findViewById(R.id.account);
        passwordText = findViewById(R.id.password);
        isRememberPwd = findViewById(R.id.login_remember);
        isAutoLogin = findViewById(R.id.login_auto);

        LitePal.getDatabase();// 建立数据库
        UserManager.clear();
    }

    void setListeners(){
        loginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });

        registerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        visitorText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 若已有游客账号则以游客身份登录，不存在则新建游客账号
                User visitor = DataSupport.where("isVisitor = ?","1")
                                          .findFirst(User.class);
                if(visitor == null){
                    visitor = new User();
                    visitor.setAccount("Visitor");
                    visitor.setPassword("Visitor");
                    visitor.setVisitor(true);
                    visitor.save();
                }
                // 保存到本地
                SharedPreferencesUtil spu = new SharedPreferencesUtil(LogInActivity.this);
                spu.setParam("account","Visitor");
                spu.setParam("pwd","Visitor");
                spu.setParam("isRememberPwd",false);
                spu.setParam("isAutoLogin",false);
                UserManager.setCurrentUser(visitor);
                autoStartActivity(MainActivity.class);
            }
        });
    }

    /**
     *  POST方式Login
     */
    private void Login() {
        // 前端参数校验，防SQL注入
        account = Util.StringHandle(accountText.getText().toString());
        password = Util.StringHandle(passwordText.getText().toString());

        // 检查数据格式是否正确
        String resMsg = checkDataValid(account,password);
        if(!resMsg.equals("")){
            showResponse(resMsg);
            return;
        }

//        progressBar.setVisibility(View.VISIBLE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("登录中...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        OptionHandle(account,password);// 处理自动登录及记住密码

        server.LoginPost(account,password,loginHandler);

        // 获取登录状态码
//        Pair<String,String> resPair = server.getLoginStatus(account,password);
//        String resCode = resPair.first;
//        resMsg = resPair.second;
//        String resCode = server.getResCode();
//        resMsg = server.getResMsg();
//        // 登录成功
//        if (resCode.equals(Consts.SUCCESSCODE_LOGIN)) {
//            // 查找本地数据库中是否已存在当前用户,不存在则新建用户并写入
//            User user = DataSupport.where("account=?",account).findFirst(User.class);
//            if(user == null){
//                user = new User();
//                user.setAccount(account);
//                user.setPassword(password);
//                user.setVisitor(false);
//                user.save();
//            }
//            UserManager.setCurrentUser(user);// 设置当前用户
//
//            autoStartActivity(MainActivity.class);
//        }
//        showResponse(resMsg);
    }

    @SuppressLint("HandlerLeak")
    Handler loginHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String resCode = server.getResCode();
                    String resMsg = server.getResMsg();
                    // 登录成功
                    if (resCode != null && resCode.equals(Consts.SUCCESSCODE_LOGIN)) {
                        // 查找本地数据库中是否已存在当前用户,不存在则新建用户并写入
                        User user = DataSupport.where("account=?",account).findFirst(User.class);
                        if(user == null){
                            user = new User();
                            user.setAccount(account);
                            user.setPassword(password);
                            user.setVisitor(false);
                            user.save();
                        }
                        UserManager.setCurrentUser(user);// 设置当前用户

                        autoStartActivity(MainActivity.class);
                    }
                    progressDialog.dismiss();// 隐藏进度条
                    showResponse(resMsg);
                    break;
            }
        }
    };

    private void showResponse(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LogInActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String checkDataValid(String account,String pwd){
        if(TextUtils.isEmpty(account) | TextUtils.isEmpty(pwd))
            return getResources().getString(R.string.null_hint);
        if(account.length() != 11 && !account.contains("@"))
            return getResources().getString(R.string.account_invalid_hint);
        return "";
    }

    void OptionHandle(String account,String pwd){
        SharedPreferences.Editor editor = getSharedPreferences("UserData",MODE_PRIVATE).edit();
        SharedPreferencesUtil spu = new SharedPreferencesUtil(this);
        // 保存账号密码
        spu.setParam("account",account);
        spu.setParam("pwd",pwd);
        if(isRememberPwd.isChecked()){
            editor.putBoolean("isRememberPwd",true);
        }else{
            editor.putBoolean("isRememberPwd",false);
        }
        if(isAutoLogin.isChecked()){
            editor.putBoolean("isAutoLogin",true);
        }else{
            editor.putBoolean("isAutoLogin",false);
        }
        editor.apply();
    }

}
