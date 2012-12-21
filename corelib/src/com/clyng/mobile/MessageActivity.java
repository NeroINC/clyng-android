package com.clyng.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import org.apache.http.protocol.HTTP;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ---
 * Date: 5/22/12
 * Time: 15:05
 */
public class MessageActivity extends Activity {

    private static final String TAG = "MessageActivity";
    private HTML5WebView _webView;
    private List<Message> _messagesList;
    private int _index;
    private Button _removeBtn;
    private Button _backBtn;
    private Button _nextBtn;
    private TextView _txtCounter;

    private boolean _fullscreen;
    private ProgressBar _progressBar;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _fullscreen = CMClient.instance().isFullScreen();

        if (_fullscreen) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        requestWindowFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        CMClient.instance().registerActivity(this);

        _messagesList = (List<Message>) getIntent().getSerializableExtra("MESSAGES");
        int startMessageId = getIntent().getIntExtra("MESSAGE_ID", 0);

        for(int i = 0; i < _messagesList.size(); i++){
            if(_messagesList.get(i).getId() == startMessageId){
                _index = i;
                break;
            }
        }

        initUi();

        displayMessage(getCurrentMessage());
    }

    private void initUi() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT)
        );

        //buttons layout

        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        );
        ((LinearLayout.LayoutParams)buttonsLayout.getLayoutParams()).setMargins(0, 4, 0, 0);

        _removeBtn = new Button(this);
        _removeBtn.setText("Remove");
        _removeBtn.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        );
        _removeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                removeMessage(getCurrentMessage());
                if(_messagesList.size() > 0){
                    displayMessage(getCurrentMessage());
                } else {
                    finish();
                }
            }
        });

        _txtCounter = new TextView(this);
        _txtCounter.setText("0 of 0");
        _txtCounter.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        );

        _backBtn = new Button(this);
        _backBtn.setText("Back");
        _backBtn.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        );
        _backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                _index--;
                displayMessage(getCurrentMessage());
            }
        });

        _nextBtn = new Button(this);
        _nextBtn.setText("Next");
        _nextBtn.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        );
        _nextBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                _index++;
                displayMessage(getCurrentMessage());
            }
        });

        if (!_fullscreen) {
            buttonsLayout.addView(_removeBtn);
        }

        View separator = new View(this);
        separator.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1)
        );
        buttonsLayout.addView(separator);

        buttonsLayout.addView(_txtCounter);

        separator = new View(this);
        separator.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1)
        );
        buttonsLayout.addView(separator);

        if (!_fullscreen) {
            buttonsLayout.addView(_backBtn);
            buttonsLayout.addView(_nextBtn);
        }

        contentLayout.addView(buttonsLayout);

        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                        _fullscreen ? ViewGroup.LayoutParams.FILL_PARENT : display.getHeight() - 100,
                        1.0f)
        );

        _webView = new HTML5WebView(this);
        _webView.setLayoutParams(
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        _fullscreen ?  display.getHeight() : display.getHeight() - 100)
        );
        /*_webView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int progress) {
                if (!_fullscreen) {
                    MessageActivity.this.setProgress(progress * 100);
                } else {
                    if (progress == 100) {
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                _progressBar.setVisibility(View.INVISIBLE);
                            }
                        }, 150);
                    } else {
                        if (_progressBar.getVisibility() != View.VISIBLE)
                            _progressBar.setVisibility(View.VISIBLE);
                    }
                }
            }
        });*/
        _webView.getSettings().setJavaScriptEnabled(true);
        _webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        _webView.getSettings().setPluginsEnabled(true);

        if (_fullscreen) {
            _webView.setOnTouchListener(swipeDetector);
        }



        TextView closeBtn = new TextView(this);
        closeBtn.setLayoutParams(
                new RelativeLayout.LayoutParams(80, 40)
        );

        String closeTitle = new String("Close");
        SpannableString content = new SpannableString(closeTitle);
        content.setSpan(new UnderlineSpan(), 0, closeTitle.length(), 0);
        closeBtn.setText(content);
        closeBtn.setTextSize(15);
        closeBtn.setClickable(true);
        ((RelativeLayout.LayoutParams) closeBtn.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onCloseButton();
            }
        });

        relativeLayout.addView(_webView.getLayout());
        relativeLayout.addView(closeBtn);

        if(_fullscreen) {
            _progressBar = new ProgressBar(this);
            _progressBar.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            params.addRule(RelativeLayout.CENTER_IN_PARENT );

            relativeLayout.addView(_progressBar, params);
        }


        contentLayout.addView(relativeLayout);

        setContentView(contentLayout);
    }

    private void onCloseButton() {
        if (CMClient.instance().isFullScreen()) {
            removeMessage(getCurrentMessage());
        }
        finish();
    }

    private void displayMessage(Message message){

       /* String str = "<!DOCTYPE html> <html> <head> <title></title> <!-- VIDEO_SCRIPT_INC_BEG --> <link href=\"http://vjs.zencdn.net/c/video-js.css\" rel=\"stylesheet\"><script src=\"http://vjs.zencdn.net/c/video.js\"></script> <!-- VIDEO_SCRIPT_INC_END --><meta name=\"viewport\" content=\"width=device-width\"/></head> <body style=\"font-family: Arial, Helvetica, sans-serif; margin-left: 0; margin-bottom: 0; margin-right: 0; margin-top: 0; padding-bottom: 0; padding-right: 20px; padding-top: 0; padding-left: 20px;\">\n" +
                "<p><video width=\"300\" height=\"200\" class=\"video-js vjs-default-skin\" controls=\"controls\" preload=\"auto\" poster=\"\" data-setup=\"{}\" src=\"https://s3.amazonaws.com/clyngvideo/oren_home/medium_2_4.mp4\"></video></p>\n" +
                "</body> </html>";
          _webView.loadData( str, "text/html", null );*/
        _webView.loadDataWithBaseURL(CMClient.instance().getServerUrl(), message.getHtml(), "text/html", HTTP.UTF_8, null);
        message.setViewed(true);
        checkActionVisibility();
        if(_index + 1 == 1 && _messagesList.size() == 1) {
            _txtCounter.setVisibility(View.INVISIBLE);
            _backBtn.setVisibility(View.INVISIBLE);
            _nextBtn.setVisibility(View.INVISIBLE);
        }
        _txtCounter.setText((_index + 1) + " of " + _messagesList.size());
        CMClient.instance().notifyMessageOpened(message);
    }

    private Message getCurrentMessage(){
        return _messagesList.get(_index);
    }

    private void checkActionVisibility(){
        _backBtn.setEnabled(_index > 0);
        _nextBtn.setEnabled(_index < _messagesList.size() - 1);
    }

    private void removeMessage(Message message){
        CMClient.instance().removeMessage(message);

        _messagesList.remove(message);
        if(_index >= _messagesList.size()){
            _index = _messagesList.size() - 1;
        }
    }

    public void finish(){
        CMClient.instance().unregisterActivity(this);
        super.finish();
    }

    private SwipeDetector swipeDetector = new SwipeDetector() {
        @Override
        public boolean onRightToLeftSwipe() {
            if (_index < _messagesList.size() -1 ) {
                _index++;
                displayMessage(getCurrentMessage());
                return true;
            }
            return false;
        }

        @Override
        public boolean onLeftToRightSwipe() {
            if (_index > 0 ) {
                _index--;
                displayMessage(getCurrentMessage());
                return true;
            }
            return false;
        }
    };

}
