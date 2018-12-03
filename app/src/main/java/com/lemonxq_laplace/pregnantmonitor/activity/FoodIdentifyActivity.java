package com.lemonxq_laplace.pregnantmonitor.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lemonxq_laplace.pregnantmonitor.Data.User;
import com.lemonxq_laplace.pregnantmonitor.R;
import com.lemonxq_laplace.pregnantmonitor.Util.CommonResponse;
import com.lemonxq_laplace.pregnantmonitor.Util.Consts;
import com.lemonxq_laplace.pregnantmonitor.Util.HttpUtil;
import com.lemonxq_laplace.pregnantmonitor.Util.PhotoUtil;
import com.lemonxq_laplace.pregnantmonitor.Util.UserManager;
import com.lemonxq_laplace.pregnantmonitor.Util.Util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author: Lemon-XQ
 * @date: 2018/4/21
 */

public class FoodIdentifyActivity extends BaseActivity {
    private ImageButton foodImg;
    private ImageView back;
    private TextView kindText;
    private TextView energyText;
    private TextView proteinText;
    private TextView carboText;
    private TextView fatText;
    private TextView fiberText;
    private TextView sugarText;
    private Toolbar toolbar;
    private PhotoUtil photoUtil = new PhotoUtil();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodidentify);
        // 伪登录（因为该功能需要进行用户校验）
        User user = new User();
        user.setAccount("914077749@qq.com");
        user.setPassword("123456");
        user.setVisitor(false);
        user.save();
        UserManager.setCurrentUser(user);// 设置当前用户

        initView();
        setListeners();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PhotoUtil.INTENT_TAKE);

    }

    void initView(){
        foodImg = findViewById(R.id.foodIdentifyImg);
        kindText = findViewById(R.id.food_kind);
        energyText = findViewById(R.id.food_energy);
        proteinText = findViewById(R.id.food_protein);
        carboText = findViewById(R.id.food_carbohydrates);
        fatText = findViewById(R.id.food_fat);
        fiberText = findViewById(R.id.food_fiber);
        sugarText = findViewById(R.id.food_sugar);
        // 标题栏设置
        toolbar = findViewById(R.id.titlebar);
        setSupportActionBar(toolbar);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.mipmap.arrow_back_white);
//        }
    }

    void setListeners(){
        foodImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPhotoDialog();
                // 裁剪尺寸及输出图片大小设置
//                photoUtil.aspectX = 450;
//                photoUtil.aspectY = 300;
                photoUtil.aspectX = 0.1f;
                photoUtil.aspectY = 0.1f;
                photoUtil.outputX = 0;
                photoUtil.outputY = 0;
//                photoUtil.outputX = foodImg.getWidth();
//                photoUtil.outputY = foodImg.getHeight();
            }
        });

        // 照片工具类回调
        photoUtil.setOnPhotoResultListener(new PhotoUtil.OnPhotoResultListener() {
            // 当选择图片或者拍摄图片拿到结果之后
            @Override
            public void onPhotoResult(final Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    final Bitmap bitmap = PhotoUtil.compressPhoto(
                            PhotoUtil.decodeUriAsBitmap(FoodIdentifyActivity.this,uri),
                            300,300);
//                    showResponse("Test_1");
                    foodImg.setImageBitmap(bitmap);// 显示食物图片
                    foodImg.setBackgroundColor(Color.WHITE);
                    // 上传食物图片
                    progressDialog = new ProgressDialog(FoodIdentifyActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(getResources().getString(R.string.identifyHint));
                    progressDialog.show();
                    HttpUtil.uploadImage(Consts.URL_FoodIdentify,"pic",bitmap,new okhttp3.Callback(){
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            progressDialog.dismiss();
                            CommonResponse res = new CommonResponse(response.body().string());
                            String resCode = res.getResCode();
                            String resMsg = res.getResMsg();
                            showResponse(resCode+resMsg);
                            HashMap<String,String> propertyMap = res.getPropertyMap();
                            // 识别成功
                            float weight = Float.parseFloat(propertyMap.get("weight"));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(weight!=-1.0f){
                                        kindText.setText(getResources().getString(R.string.food_rice));
                                        double energy = (weight/100.0)*116;
                                        double protein = (weight/100.0)*2.6;
                                        double carbo = (weight/100.0)*25.6;
                                        double fat = (weight/100.0)*3;
                                        double fiber = (weight/100.0)*0.3;
                                        double sugar = (weight/100.0)*5.8;
                                        DecimalFormat decimalFormat = new DecimalFormat(".00");// 如果小数不足2位,会以0补足.
                                        energyText.setText(decimalFormat.format(energy));
                                        proteinText.setText(decimalFormat.format(protein));
                                        carboText.setText(decimalFormat.format(carbo));
                                        fatText.setText(decimalFormat.format(fat));
                                        fiberText.setText(decimalFormat.format(fiber));
                                        sugarText.setText(decimalFormat.format(sugar));
                                    }else{
                                        kindText.setText("未知");
                                        energyText.setText("0.0");
                                        proteinText.setText("0.0");
                                        carboText.setText("0.0");
                                        fatText.setText("0.0");
                                        fiberText.setText("0.0");
                                        sugarText.setText("0.0");
                                    }
                                }
                            });
                            showResponse(resMsg);
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            showResponse("Network ERROR!");
                            progressDialog.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onPhotoCancel() {

            }
        });

    }

    void showPhotoDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(FoodIdentifyActivity.this);
        builder.setTitle(getResources().getString(R.string.Upload_food_picture));
        final String[] choices = {getResources().getString(R.string.takePhoto), getResources().getString(R.string.Select_from_album)};
        // 设置一个下拉的列表选择项
        builder.setItems(choices, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch(which)
                {
                    case 0:// 相机
                        photoUtil.takePicture(FoodIdentifyActivity.this);
                        break;
                    case 1:// 本地相册
                        photoUtil.selectPicture(FoodIdentifyActivity.this);
                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PhotoUtil.INTENT_CROP:
            case PhotoUtil.INTENT_TAKE:
            case PhotoUtil.INTENT_SELECT:
                photoUtil.onActivityResult(this, requestCode, resultCode, data);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showResponse(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FoodIdentifyActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
