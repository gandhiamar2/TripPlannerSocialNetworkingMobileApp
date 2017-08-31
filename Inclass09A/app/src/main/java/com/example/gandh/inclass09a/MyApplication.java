package com.example.gandh.inclass09a;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by gandh on 5/1/2017.
 */

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
