package com.lemonxq.foodidentify;

import com.lemonxq.foodidentify.Util.CrashHandler;

/**
 * Created by LemonXQ on 2018/12/4.
 */

public class CustomApplication extends org.litepal.LitePalApplication{
    private CrashHandler mCrashHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(getApplicationContext(), getClass());

    }
}
