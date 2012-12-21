package com.clyng.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.clyng.mobile.CMClient;
import com.clyng.mobile.CMClientListener;

/**
 * Created by IntelliJ IDEA.
 *
 * @author alximik
 * @since 8/1/12 4:58 PM
 */
public class LoginActivity extends Activity {

    Handler handler;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();

        CMClient.init(this);

         AppSettings settings = AppSettings.load();
        if (settings.getCustomerKey() != null)
            CMClient.instance().setCustomerKey( settings.getCustomerKey() );

        if (settings.getEmail() != null)
            CMClient.instance().setEmail( settings.getEmail() );

        if (settings.getServerUrl() != null)
            CMClient.instance().setServerUrl( settings.getServerUrl() );

        if (settings.getUserId() != null)
            CMClient.instance().setUserId( settings.getUserId() );

        CMClient.instance().setFullScreen( settings.isFullscreen() );

        setContentView(R.layout.login);

        final EditText txtServer = (EditText) findViewById(R.id.txtServer);
        txtServer.setText(CMClient.instance().getServerUrl());

        final EditText txtUserId = (EditText) findViewById(R.id.txtUserId);
        txtUserId.setText( CMClient.instance().getUserId() );

        final EditText txtPassword = (EditText) findViewById(R.id.txtPassword);

        final EditText txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtEmail.setText( CMClient.instance().getEmail() );

        TextView version = (TextView) findViewById(R.id.version);
        try {
            version.setText("Version: " + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
        }

        Button btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                CMClient.instance().setServerUrl(txtServer.getText().toString());
                CMClient.instance().setUserId( txtUserId.getText().toString() );
                CMClient.instance().setEmail( txtEmail.getText().toString() );

                AppSettings settings = AppSettings.load();
                settings.setServerUrl( txtServer.getText().toString() );
                settings.setUserId( txtUserId.getText().toString() );
                settings.setEmail( txtEmail.getText().toString() );
                settings.save();

                CMClient.instance().registerUser(new CMClientListener() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "onSuccess() registerUser");

                    }

                    @Override
                    public void onError(Exception e) {
                        Log.i(TAG, "onError() registerUser; e: " + e);
                        LoginActivity.this.showAlert( e.toString() );
                    }
                });
                startActivity(new Intent(LoginActivity.this, MainActivity.class));

            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        final EditText txtUserId = (EditText) findViewById(R.id.txtUserId);
        txtUserId.setText( CMClient.instance().getUserId() );

    }

    void showAlert( String text )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(text)
                .setTitle("Error").setPositiveButton("Close", null);

        AlertDialog dialog = builder.create();

        dialog.show();
    }
}
