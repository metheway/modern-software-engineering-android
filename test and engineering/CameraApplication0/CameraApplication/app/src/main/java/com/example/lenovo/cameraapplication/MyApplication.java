package com.example.lenovo.cameraapplication;

import android.app.Application;
import android.content.Context;

/**
 * Created by lenovo on 2018/10/11.
 */

public class MyApplication extends Application{
    private  static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
    }
    public static Context getContext() {
        return context;
    }
}
