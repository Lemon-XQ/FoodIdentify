package com.lemonxq.foodidentify.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lemonxq.foodidentify.Util.ActivityController;

/**
 * @author: Lemon-XQ
 * @date: 2018/2/9
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ActivityController.addActivity(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        ActivityController.removeActivity(this);
    }

    void autoStartActivity(Class T){
        Intent intent = new Intent(this,T);
        startActivity(intent);
    }

    void autoStartActivityForResult(Class T,int requestCode){
        Intent intent = new Intent(this,T);
        startActivityForResult(intent,requestCode);
    }

}
