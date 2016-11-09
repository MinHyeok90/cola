package com.example.android.cola;

import android.app.Application;

import io.paperdb.Paper;

public class ColaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Paper.init(getApplicationContext());
    }
}
