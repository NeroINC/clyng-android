package com.clyng.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.clyng.mobile.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    private Handler _handler = new Handler();
    private EditText txtCustomerToken;
    private static final String TAG = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        CMClient.instance().setCMClientListener(new CMClientListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "onSuccess()");
            }

            @Override
            public void onError(Exception e) {
                Log.i(TAG, "onError(); e: " + e);
                MainActivity.this.showAlert(e.toString());
            }
        });

        Button signIn = (Button) findViewById(R.id.btnSignIn);
        signIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                CMClient.instance().sendEvent("sign-in", null, null);
            }
        });

        Button signOut = (Button) findViewById(R.id.btnSignOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                CMClient.instance().sendEvent("sign-out", null, null);
            }
        });

        Button share = (Button) findViewById(R.id.btnShare);
        share.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                prompt(new String[] { "shared-with" }, new PromptListener() {
                    public void onDialogConfirm(String[] values) {
                        final Map<String,Object> map = new HashMap<String, Object>();
                        map.put("shared-with", values[0]);
                        CMClient.instance().sendEvent("share", map, null);
                    }
                });
            }
        });

        Button custom = (Button) findViewById(R.id.btnCustom);
        custom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                prompt(new String[] { "Event name", "Key 1", "Value 1", "Key 2", "Value 2", "Key 3", "Value 3" }, new PromptListener() {
                    public void onDialogConfirm(String[] values) {
                        final Map<String,Object> map = new HashMap<String, Object>();

                        if( values[2].length() > 0 )
                            map.put(values[1], values[2]);
                        if( values[4].length() > 0 )
                            map.put(values[3], values[4]);
                        if( values[6].length() > 0 )
                            map.put(values[5], values[6]);

                        CMClient.instance().sendEvent(values[0], map, null);
                    }
                });
            }
        });

        Button btnSetValue = (Button) findViewById(R.id.btnSetValue);
        btnSetValue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                prompt2(new String[] { "string", "double", "boolean", "date" }, new PromptListener() {
                    public void onDialogConfirm(String[] values) {
                        final Map<String,Object> map = new HashMap<String, Object>();

                        for( int i = 0; i < values.length / 2; i++ )
                        {
                            String name = values[i*2 + 0];
                            String value = values[i*2 + 1];

                            if( name.length() > 0 && value.length() > 0 )
                            {
                                switch( i )
                                {
                                    case 0: // string
                                        CMClient.instance().setValue(name, value, 5000, null);
                                        break;
                                    case 1: // double
                                        CMClient.instance().setValue(name, Double.valueOf(value), 0, null);
                                        break;
                                    case 2: // boolean

                                        CMClient.instance().setValue(name, Boolean.valueOf(value), 5000, null);
                                        break;
                                    case 3: // date
                                    {
                                        SimpleDateFormat sdt = new SimpleDateFormat("yyyy.MM.dd");
                                        try {
                                            Date dt = sdt.parse(value);
                                            CMClient.instance().setValue(name, dt, 5000, null);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                        break;
                                }
                            }
                        }
                    }
                });
            }
        });

        Button btnChangeUser = (Button) findViewById(R.id.btnChangeUser);
        btnChangeUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                prompt(new String[] { "New user id" }, new PromptListener() {
                    public void onDialogConfirm(String[] values) {
                        final Map<String,Object> map = new HashMap<String, Object>();

                        if( values[0].length() > 0 )
                        {
                            CMClient.instance().changeUserId(values[0], 5000, null);

                            AppSettings settings = AppSettings.load();
                            settings.setUserId( values[0] );
                            settings.save();
                            CMClient.instance().setUserId( values[0] );
                        }
                    }
                });
            }
        });

     /*   Button btnChangeDate = (Button) findViewById(R.id.btnChangeDate);
        btnChangeDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                prompt(new String[] { "New date (yyyy.MM.dd)" }, new PromptListener() {
                    public void onDialogConfirm(String[] values) {
                        final Map<String,Object> map = new HashMap<String, Object>();

                        if( values[0].length() > 0 )
                            CMClient.instance().changeDate(values[0]);
                    }
                });
            }
        });*/

        Button btnPending = (Button) findViewById(R.id.btnPending);
        btnPending.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                CMClient.instance().getPendingMessages(null);
            }
        });

        RadioGroup rdgMethod = (RadioGroup) findViewById(R.id.rdgMethod);
        rdgMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                CMClient.instance().setFullScreen(radioGroup.getCheckedRadioButtonId() == R.id.rdbFullscreen);
                AppSettings settings = AppSettings.load();
                settings.setFullscreen( radioGroup.getCheckedRadioButtonId() == R.id.rdbFullscreen );
                settings.save();

            }
        });
        if (CMClient.instance().isFullScreen()) {
            RadioButton btn = (RadioButton) findViewById(R.id.rdbFullscreen);
            btn.setChecked(true);
        } else {
            RadioButton btn = (RadioButton) findViewById(R.id.rdbRegular);
            btn.setChecked(true);
        }

        txtCustomerToken = (EditText) findViewById(R.id.device_token);
        txtCustomerToken.setText(CMClient.instance().getCustomerKey());

        Button btnCustomerKey = (Button) findViewById(R.id.btnSetCustomerKey);
        btnCustomerKey.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                CMClient.instance().setCustomerKey( txtCustomerToken.getText().toString() );
                AppSettings settings = AppSettings.load();
                settings.setCustomerKey( txtCustomerToken.getText().toString() );
                settings.save();

                CMClient.instance().registerUser(null);
            }
        });

        Button btnUnregister = (Button) findViewById(R.id.btnUnregister);
        btnUnregister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CMClient.instance().unregisterUser(null);
            }
        });

        //updateTokenLabel();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        Button signIn = (Button) findViewById(R.id.btnSignIn);
//        signIn.setFocusableInTouchMode(true);
//        signIn.requestFocus();

        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(txtCustomerToken, 0);
    }

    private void prompt(String[] fieldNames, final PromptListener listener){
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT)
        );

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT)
        );

        final List<EditText> fields = new ArrayList<EditText>(fieldNames.length);

        for(String field : fieldNames){
            TextView textView = new TextView(this);
            textView.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            );
            textView.setText(field);
            contentLayout.addView(textView);

            EditText editText = new EditText(this);
            editText.setMaxLines(1);
            editText.setSingleLine(true);
            editText.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            );
            contentLayout.addView(editText);
            fields.add(editText);
        }

        scrollView.addView(contentLayout);

        new AlertDialog.Builder(this)
                .setTitle("Input params")
                .setView(scrollView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        List<String> values = new ArrayList<String>();
                        for (EditText field : fields) {
                            values.add(field.getText().toString());
                        }
                        listener.onDialogConfirm(values.toArray(new String[values.size()]));
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        }).show();

    }

    private void prompt2(String[] fieldNames, final PromptListener listener){
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT)
        );

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT)
        );

        final List<EditText> fields = new ArrayList<EditText>(fieldNames.length*2);

        for(String field : fieldNames){
            LinearLayout line = new LinearLayout(this);
            line.setOrientation( LinearLayout.HORIZONTAL );
            line.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            );
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f);
            lp.setMargins(5, 0, 0, 0);
            textView.setLayoutParams(
                    lp
            );

            textView.setText(field);
            line.addView(textView);

            EditText editText1 = new EditText(this);
            editText1.setMaxLines(1);
            editText1.setHint("name");
            editText1.setSingleLine(true);
            editText1.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f)
            );
            line.addView(editText1);
            fields.add(editText1);

            EditText editText2 = new EditText(this);
            editText2.setMaxLines(1);
            editText2.setHint("value");
            editText2.setSingleLine(true);
            editText2.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f)
            );
            line.addView(editText2);
            fields.add(editText2);

            contentLayout.addView(line);
        }

        scrollView.addView(contentLayout);

        new AlertDialog.Builder(this)
                .setTitle("Input params")
                .setView(scrollView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        List<String> values = new ArrayList<String>();
                        for (EditText field : fields) {
                            values.add(field.getText().toString());
                        }
                        listener.onDialogConfirm(values.toArray(new String[values.size()]));
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        }).show();

    }

    private interface PromptListener {
        void onDialogConfirm(String[] values);
    }

    void showAlert( String text )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(text)
                .setTitle("Error");

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
