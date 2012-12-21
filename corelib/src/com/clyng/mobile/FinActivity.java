package com.clyng.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by IntelliJ IDEA.
 *
 * @author alximik
 * @since 8/2/12 10:56 PM
 */
public class FinActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }

    public static void closeApplication(Activity context) {
        Intent intent = new Intent(context, FinActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        context.finish();
    }
}
