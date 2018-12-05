package com.lemonxq.foodidentify.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener;
import com.lemonxq.foodidentify.R;
import com.lemonxq.foodidentify.Util.Consts;
import com.lemonxq.foodidentify.Util.Util;
import com.lemonxq.foodidentify.view.BaseDialog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author: Lemon-XQ
 * @date: 2018/2/24
 */

public class AboutActivity extends BaseActivity {
    private ImageView back;
    private TextView versionText;
    private Button checkUpdateBtn;
    private Button feedbackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initView();
        setListeners();
    }
    void initView(){
        back = findViewById(R.id.back);
        versionText = findViewById(R.id.about_version);
        checkUpdateBtn = findViewById(R.id.about_checkUpdate);
        feedbackBtn = findViewById(R.id.about_feedback);
        String versionCode = "V"+Util.getLocalVersionCode(this)+".0";// 版本号
        versionText.setText(versionCode);
    }

    void setListeners(){
        // 设置返回键监听
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 检查更新
        checkUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUpdate();
            }
        });

        // 反馈
        feedbackBtn.setOnClickListener((view)->{
            Intent i = new Intent(Intent.ACTION_SEND);
            // i.setType("text/plain"); //模拟器
            i.setType("message/rfc822"); // 真机
            i.putExtra(Intent.EXTRA_EMAIL,
                    new String[] { "lemonxq1997@gmail.com" });
            i.putExtra(Intent.EXTRA_SUBJECT, "[安心孕APP意见反馈]");
            i.putExtra(Intent.EXTRA_TEXT, "期待收到您的建议");
            startActivity(Intent.createChooser(i,
                    "通过邮件发送"));
        });
    }

    void checkUpdate(){
        DownloadBuilder mBuilder = AllenVersionChecker
                .getInstance()
                .requestVersion()
                .setRequestUrl(Consts.URL_Update)
                .request(new RequestVersionListener() {
                    @Nullable
                    @Override
                    public UIData onRequestVersionSuccess(String result) {
                        String downloadUrl="",versionDes="";
                        double versionCode=0;
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            downloadUrl = jsonObject.getString("downloadUrl");
                            versionDes = jsonObject.getString("versionDes");
                            versionCode = jsonObject.getDouble("versionCode");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // 服务端版本大于本地版本时才进行更新
                        if(versionCode <= Util.getLocalVersionCode(AboutActivity.this)){
                            Util.makeToast(AboutActivity.this,"当前已是最新版本");
                            return null;
                        }
                        return UIData
                                .create()
                                .setDownloadUrl(downloadUrl)
                                .setContent(versionDes);
                    }

                    @Override
                    public void onRequestVersionFailure(String message) {
                        Util.makeToast(AboutActivity.this,"Network ERROR!Request Failed");
                    }
                });
        mBuilder.setCustomVersionDialogListener((context, versionBundle) -> {
            BaseDialog baseDialog = new BaseDialog(context, R.style.BaseDialog,R.layout.dialog_update);
            TextView textView = baseDialog.findViewById(R.id.tv_msg);
            textView.setText(versionBundle.getContent());
            return baseDialog;
        });
        mBuilder.setForceRedownload(true);
        mBuilder.excuteMission(this);
    }

}
