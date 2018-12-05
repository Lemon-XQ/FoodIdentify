package com.lemonxq.foodidentify.Util;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.lemonxq.foodidentify.Data.Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author: Lemon-XQ
 * @date: 2018/2/23
 */

public class Server {
    private Activity mActivity;
    private String resCode;
    private String resMsg;
    private HashMap<String,String> property;
    private ArrayList<HashMap<String,String>> dataList;
    private final long sleepTime = 750L;

    public Server(Activity activity){
        mActivity = activity;
    }

    public String getResCode() {
        return resCode;
    }

    public String getResMsg() {
        return resMsg;
    }

    public void LoginPost(String account, String password, final Handler mHandler){
        final CommonRequest request = new CommonRequest();
        // 填充参数
        request.addRequestParam("account",account);
        request.addRequestParam("pwd",password);

        infoPost(Consts.URL_Login, request.getJsonStr());
        // 隔一段时间（2.5s）后再发送信息给LogInActivity，因为网络请求是耗时操作
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                mHandler.sendMessage(message);
            }
        }, 2500);
    }

    // ----------------下为User各个属性的获取及设置（服务器端）---------------
    public String getNickname(){
        CommonRequest request = new CommonRequest();
        request.setRequestCode(Consts.REQUESTCODE_NICKNAME);
        request.addRequestParam("account",UserManager.getCurrentUser().getAccount());
        try {
            infoPost(Consts.URL_GetInfo, request.getJsonStr());
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(property != null){
            return property.get("nickname");
//            property.clear();
        }
        return null;
    }

    public void getNicknamePost(final Handler mHandler){
        CommonRequest request = new CommonRequest();
        request.setRequestCode(Consts.REQUESTCODE_NICKNAME);
        request.addRequestParam("account",UserManager.getCurrentUser().getAccount());
        infoPost(Consts.URL_GetInfo, request.getJsonStr());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("height",property.get("nickname"));
                msg.setData(bundle);
                msg.what = 1;
                mHandler.sendMessage(msg);
                property.clear();
            }
        },1500L);
    }

    public void getHeightPost(final Handler mHandler){
        CommonRequest request = new CommonRequest();
        request.setRequestCode(Consts.REQUESTCODE_HEIGHT);
        request.addRequestParam("account",UserManager.getCurrentUser().getAccount());
        infoPost(Consts.URL_GetInfo, request.getJsonStr());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putFloat("height",Float.parseFloat(property.get("height")));
                msg.setData(bundle);
                msg.what = 1;
                mHandler.sendMessage(msg);
                property.clear();
            }
        },2000L);
    }

    public String getHeight(){
        CommonRequest request = new CommonRequest();
        request.setRequestCode(Consts.REQUESTCODE_HEIGHT);
        request.addRequestParam("account",UserManager.getCurrentUser().getAccount());
        try {
            infoPost(Consts.URL_GetInfo, request.getJsonStr());
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(property != null){
            return property.get("height");
        }
//        property.clear();
        return null;
    }

    

    public ArrayList<Record> getRecords(){
        CommonRequest request = new CommonRequest();
        request.setRequestCode(Consts.REQUESTCODE_RECORD);
        request.addRequestParam("account",UserManager.getCurrentUser().getAccount());
        try {
            infoPost(Consts.URL_GetInfo, request.getJsonStr());
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<Record> recordList = new ArrayList<>();
        if(dataList == null) return recordList;
        for(HashMap<String,String> dataMap:dataList){
            Record record = new Record();
            record.setUser(UserManager.getCurrentUser());
            record.setHeight(Float.parseFloat(dataMap.get("height")));
            record.setWeight(Float.parseFloat(dataMap.get("weight")));
            record.setDate(new Date(Long.parseLong(dataMap.get("date"))));
            record.setHealthy(Boolean.parseBoolean(dataMap.get("isHealthy")));
            record.setAge(Integer.parseInt(dataMap.get("age")));
            record.setOgtt(Float.parseFloat(dataMap.get("ogtt")));
            record.save();// 同时存入本地数据库
            recordList.add(record);
        }
        dataList.clear();
        return recordList;
    }

    public Date getBirthDate(){
        CommonRequest request = new CommonRequest();
        request.setRequestCode(Consts.REQUESTCODE_BIRTHDATE);
        request.addRequestParam("account",UserManager.getCurrentUser().getAccount());
        try {
            infoPost(Consts.URL_GetInfo, request.getJsonStr());
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(property != null){
            String dateStr = property.get("birthDate");
            property.clear();
            if(dateStr!=null){
                return new Date(Long.parseLong(dateStr));// string 转 Date
            }
            else return null;
        }else{
            return null;
        }
    }

    public Date getPregnantDate(){
        CommonRequest request = new CommonRequest();
        request.setRequestCode(Consts.REQUESTCODE_PREGNANTDATE);
        request.addRequestParam("account",UserManager.getCurrentUser().getAccount());
        try {
            infoPost(Consts.URL_GetInfo, request.getJsonStr());
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(property != null){
            String dateStr = property.get("pregnantDate");
            property.clear();
            if(dateStr!=null){
                return new Date(Long.parseLong(dateStr));// string 转 Date
            }
            else return null;
        }else{
            return null;
        }
    }

    public String setBirthDate(Date birthDate){
        CommonRequest request = new CommonRequest();
        request.setRequestCode(Consts.REQUESTCODE_BIRTHDATE);
        request.addRequestParam("account",UserManager.getCurrentUser().getAccount());
        request.addRequestParam("birthDate",birthDate.getTime()+"");
        try {
            infoPost(Consts.URL_SetInfo,request.getJsonStr());
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resCode;
    }

    public String setPregnantDate(Date pregnantDate){
        CommonRequest request = new CommonRequest();
        request.setRequestCode(Consts.REQUESTCODE_PREGNANTDATE);
        request.addRequestParam("account",UserManager.getCurrentUser().getAccount());
        request.addRequestParam("pregnantDate",pregnantDate.getTime()+"");
        try {
            infoPost(Consts.URL_SetInfo,request.getJsonStr());
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resCode;
    }

    public String setNickname(String nickname){
        CommonRequest request = new CommonRequest();
        request.setRequestCode(Consts.REQUESTCODE_NICKNAME);
        request.addRequestParam("account",UserManager.getCurrentUser().getAccount());
        request.addRequestParam("nickname",nickname);
        try {
            infoPost(Consts.URL_SetInfo,request.getJsonStr());
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resCode;
    }

    public String setHeight(String height){
        CommonRequest request = new CommonRequest();
        request.setRequestCode(Consts.REQUESTCODE_HEIGHT);
        request.addRequestParam("account",UserManager.getCurrentUser().getAccount());
        request.addRequestParam("height",height);
        try {
            infoPost(Consts.URL_SetInfo,request.getJsonStr());
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resCode;
    }

    public String setRecord(Record record){
        CommonRequest request = new CommonRequest();
        request.setRequestCode(Consts.REQUESTCODE_RECORD);
        request.addRequestParam("date",record.getDate().getTime()+"");
        request.addRequestParam("account",record.getUser().getAccount());
        request.addRequestParam("height",record.getHeight()+"");
        request.addRequestParam("weight",record.getWeight()+"");
        request.addRequestParam("ogtt",record.getOgtt()+"");
        request.addRequestParam("age",record.getAge()+"");
        request.addRequestParam("isHealthy",record.isHealthy()+"");
        try {
            infoPost(Consts.URL_SetInfo,request.getJsonStr());
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resCode;
    }

    private void infoPost(String url, String json){
        HttpUtil.sendPost(url,json,new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                CommonResponse res = new CommonResponse(response.body().string());
                resCode = res.getResCode();
                resMsg = res.getResMsg();
                property = res.getPropertyMap();
                dataList = res.getDataList();
//                showResponse(resMsg);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                showResponse("Network ERROR");
            }
        });
    }

    private void showResponse(final String msg) {
        Log.e("Server",msg);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
