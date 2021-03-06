package com.lemonxq.foodidentify.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lemonxq.foodidentify.R;
import com.lemonxq.foodidentify.adapter.CommonAdapter;
import com.lemonxq.foodidentify.adapter.CommonViewHolder;
import com.lemonxq.foodidentify.step.bean.StepData;
import com.lemonxq.foodidentify.step.utils.DbUtils;

import java.util.List;


/**
 * @author: Lemon-XQ
 * @date: 2018/2/8
 */

public class HistoryActivity extends BaseActivity {
    private ImageView iv_left;
    private ListView lv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initView();
        iv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });
        initData();
    }

    private void initView() {
        iv_left = (ImageView) findViewById(R.id.iv_left);
        lv = (ListView) findViewById(R.id.lv);
    }

    private void initData() {
        setEmptyView(lv);
        if(DbUtils.getLiteOrm() == null){
            DbUtils.createDb(this, "FootstepData");
        }
        List<StepData> stepDatas =DbUtils.getQueryAll(StepData.class);

        lv.setAdapter(new CommonAdapter<StepData>(this,stepDatas,R.layout.item) {
            @Override
            protected void convertView(View item, StepData stepData) {
                TextView tv_date= CommonViewHolder.get(item,R.id.tv_date);
                TextView tv_step= CommonViewHolder.get(item,R.id.tv_step);
                tv_date.setText(stepData.getToday());
                tv_step.setText(stepData.getStep()+"步");
            }
        });
    }

    protected <T extends View> T setEmptyView(ListView listView) {
        TextView emptyView = new TextView(this);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        emptyView.setText("暂无数据！");
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        emptyView.setVisibility(View.GONE);
        ((ViewGroup) listView.getParent()).addView(emptyView);
        listView.setEmptyView(emptyView);
        return (T) emptyView;
    }
}
