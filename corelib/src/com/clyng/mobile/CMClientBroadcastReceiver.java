package com.clyng.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by IntelliJ IDEA.
 * User: ---
 * Date: 5/22/12
 * Time: 14:28
 */
public class CMClientBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        CMClient.instance().setContext(context);
        CMClient.instance().handleIntent(intent);
    }
}
