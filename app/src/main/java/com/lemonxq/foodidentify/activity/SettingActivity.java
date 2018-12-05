package com.lemonxq.foodidentify.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.lemonxq.foodidentify.R;
import com.lemonxq.foodidentify.fragment.SettingFragment;

public class SettingActivity extends BaseActivity {

    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_setting);

        // 动态添加碎片
        getFragmentManager().beginTransaction().replace(R.id.settingContainer,
                new SettingFragment()).commit();

        // 设置返回键监听
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
