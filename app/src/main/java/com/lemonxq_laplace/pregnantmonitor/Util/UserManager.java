package com.lemonxq_laplace.pregnantmonitor.Util;

import android.util.Log;

import com.lemonxq_laplace.pregnantmonitor.Data.User;

import org.litepal.LitePalApplication;

/**
 * @author: Lemon-XQ
 * @date: 2018/2/10
 */

public class UserManager {
    private static User mUser;

    public static User getCurrentUser(){
        if(mUser == null){
            SharedPreferencesUtil spu = new SharedPreferencesUtil(LitePalApplication.getContext());
            String account = (String) spu.getParam("account","");
            if(account.equals("")){
                Log.e("UserManager","account not found");
                mUser = new User();
            }
            else
                mUser = Database.findUserByName(account);
        }
        return mUser;
    }

    public static void setCurrentUser(User user){
        mUser = user;
    }

    public static void clear(){
        mUser = null;
    }
}
