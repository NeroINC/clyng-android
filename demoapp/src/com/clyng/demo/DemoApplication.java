package com.clyng.demo;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.clyng.mobile.CMClient;


/**
 * Created by IntelliJ IDEA.
 * User: ---
 * Date: 5/22/12
 * Time: 14:36
 */
public class DemoApplication extends Application {

    private static Context context;

    public DemoApplication() {
        super();
        context = this;
    }

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
        registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
        registrationIntent.putExtra("sender", "40483712696");
        this.startService(registrationIntent);


    }
}
