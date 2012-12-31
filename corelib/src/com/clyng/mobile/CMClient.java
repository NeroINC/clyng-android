package com.clyng.mobile;

import android.R;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ---
 * Date: 5/21/12
 * Time: 23:08
 */
public class CMClient {

    private static final int MESSAGE_FILTER = 1;

    public static final String Tablet = "Tablet";
    public static final String Phone = "Phone";

    public static final String serverUrl = "serverUrl";
    public static final String apiKey = "apiKey";
    public static final String appName = "appName";
//    public static final String useGpsLocation = "useGpsLocation";
//    public static final String UserId = "UserId";
//    public static final String Email = "Email";
//    public static final String CmLocale = "cmLocale";

    private static final String SETTINGS_FILE_NAME = "Clyng_Settings.file";
    private static final String DEFAULT_PLIST_NAME = "clyng_config.properties";
    private static final String DEVICE_TOKEN_PREF = "Clyng_DeviceToken";

    private static CMClient _instance;
    private CMClientListener listener;
    private boolean _fullScreen = false;
    private static final String TAG = "CMClient";

    public void setAppName(String _appName) {
        this._appName = _appName;
    }

    private String _appName;

    /**
     * Initialization of CMCClient. Should be called before instance() call.
     *
     * @param context    instance of Context. Can't be null
     * @param properties application properties such as: serverUrl, customerKey, useGpsLocation, UserId ...
     * @return new instance of CMCClient
     */
    public static synchronized CMClient init(Context context, Properties properties) {
//        ensureInstanceState(false);
        _instance = new CMClient(context, properties);
        return _instance;
    }

    /**
     * Initialization of CMCClient using default properties list. Should be called before instance() call.
     *
     * @param context instance of Context. Can't be null
     * @return new instance of CMCClient
     */
    public static CMClient init(Context context) {
        return init(context, DEFAULT_PLIST_NAME);
    }

    /**
     * Initialization of CMCClient. Should be called before instance() call.
     *
     * @param context            instance of Context. Can't be null
     * @param propertiesFilePath path to file with properties
     * @return new instance of CMCClient
     */
    public static CMClient init(Context context, String propertiesFilePath) {
        Properties properties = null;
        InputStream stream = null;
        try {
            AssetManager assetManager = context.getResources().getAssets();
            properties = new Properties();
            stream = assetManager.open(propertiesFilePath);
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
            properties = null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return init(context, properties);
    }


    /**
     * Set CMCClient Listener
     *
     * @param clientListener - listener
     */
    public void setCMClientListener(CMClientListener clientListener) {
        listener = clientListener;
    }

    private static void ensureInstanceState(boolean shouldBeNotNull) {
        if (_instance == null && shouldBeNotNull) {
            throw new IllegalStateException("Instance not initialized");
        }

        if (_instance != null && !shouldBeNotNull) {
            throw new IllegalStateException("Instance already initialized");
        }
    }

    /**
     * Get instance of client. Init should be called first.
     *
     * @return instance of CMCClient
     * @throws IllegalStateException if instance was not initialized
     */
    public static CMClient instance() {
        ensureInstanceState(true);
        return _instance;
    }


    private Activity _currentActivity;
    private Context _context;
    private String _userId;
    private String _serverUrl;
    private String _customerKey;
    private String _email;
    private String _locale;
    private boolean _useGps;
    private Location _location;
    private WebClient _webClient;
    private LocationListener _locationListener = new LocationListenerImpl();
    private Location _detectedLocation;
    private Timer _locationProviderTimer;

    private Handler _handler = new Handler();
    private LocationManager _locationManager;
    private ArrayList<Message> _messageList = new ArrayList<Message>(10);

    public void setContext(Context context) {
        _context = context;
    }

    private CMClient(Context context, Properties properties) {
        if (context == null) {
            throw new NullPointerException("context can't be null");
        }
        _context = context;
        Log.i(TAG, "CMClient();  properties= " + properties);
        if (properties != null) {
            _serverUrl = properties.getProperty(serverUrl);
            _customerKey = properties.getProperty(apiKey);
            _appName = properties.getProperty(appName);
            if (_appName == null) {
                _appName = _context.getPackageName();
            }
            Log.i(TAG, "CMClient();  _serverUrl= " + _serverUrl);
        }

        _webClient = new WebClient(this);
        _locationManager = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
        if (_useGps) {
            requestLocationUpdates();
        }
    }

    private void sendResponseSuccess(CMClientListener clientListener) {
        if (clientListener != null) {
            clientListener.onSuccess();
        } else if (listener != null) {
            listener.onSuccess();
        }
    }

    private void sendResponseError(CMClientListener clientListener, Exception error) {
        if (clientListener != null) {
            clientListener.onError(error);
        } else if (listener != null) {
            listener.onError(error);
        }
    }

    private void sendResponse(CMClientListener clientListener, Exception error) {
        if (error != null) {
            sendResponseError(clientListener, error);
        } else {
            sendResponseSuccess(clientListener);
        }
    }

    /**
     * Register new user
     *
     * @param clientListener If null - used listener that have been set by calling method <b>setCMCClientListener(CMClientListener clientListener)</b>
     * @throws Exception
     */
    public void registerUser(final CMClientListener clientListener) {
        _webClient.registerUser(new WebClientListener() {
            @Override
            public void response(Object data, Exception error) {
                sendResponse(clientListener, error);
            }
        });
    }

    /**
     * Unregister user
     *
     * @param clientListener If null - used listener that have been set by calling method <b>setCMCClientListener(CMClientListener clientListener)</b>
     * @throws Exception
     */
    public void unregisterUser(final CMClientListener clientListener) {
        _webClient.unregisterUser(new WebClientListener() {
            @Override
            public void response(Object data, Exception error) {
                sendResponse(clientListener, error);
            }
        });
    }

    /**
     * Sends event
     *
     * @param event          event name
     * @param data
     * @param clientListener If null - used listener that have been set by calling method <b>setCMCClientListener(CMClientListener clientListener)</b>
     * @throws Exception
     */
    public void sendEvent(final String event, final Map<String, Object> data, final CMClientListener clientListener) {
        if (this._useGps && this.isLocationOutdated(_detectedLocation)) {
            requestLocationUpdates();
        }

        _webClient.sendEvent(event, data, new WebClientListener() {
            @Override
            public void response(Object data, Exception error) {
                sendResponse(clientListener, error);
            }
        });
    }

    /**
     * @return is full screen state
     */
    public boolean isFullScreen() {
        return _fullScreen;
    }

    /**
     * Set up screen state
     *
     * @param fullscreen screen state
     */
    public void setFullScreen(boolean fullscreen) {
        this._fullScreen = fullscreen;
    }

    /**
     * @return current user id
     */
    public String getUserId() {
        return _userId;
    }

    /**
     * Set up id of the user
     *
     * @param userId new user id
     */
    public void setUserId(String userId) {
        _messageList.clear();
        _userId = userId;
    }

    /**
     * Retrieve current Url of the server
     *
     * @return server Url
     */
    public String getServerUrl() {
        Log.i(TAG, "_serverUrl= " + _serverUrl);
        return _serverUrl;
    }

    /**
     * Set up Url of the server
     *
     * @param serverUrl new Url of the server
     */
    public void setServerUrl(String serverUrl) {
        _serverUrl = serverUrl != null ? serverUrl.trim() : null;
        Log.i(TAG, "setServerUrl();  _serverUrl= " + _serverUrl);
    }

    /**
     * Retrieve Customer's Key
     *
     * @return Customer Key
     * @deprecated use getApiKey()
     */
    public String getCustomerKey() {
        return getApiKey();
    }


    public String getApiKey() {
        return _customerKey;
    }


    /**
     * Set up Customer's Key
     * @deprecated use setApiKey(...)
     * @param customerKey - new customer's key
     */
    public void setCustomerKey(String customerKey) {
        setApiKey(customerKey);
    }

    public void setApiKey(String apiKey) {
        _customerKey = apiKey;
    }

    /**
     * Retrieve Email
     *
     * @return email
     */
    public String getEmail() {
        return _email;
    }

    /**
     * Set up Email
     *
     * @param email - new email
     */
    public void setEmail(String email) {
        _email = email;
    }

    /**
     * Retrieve current locale language
     *
     * @return current device language
     */
    public String getLocale() {
        if (_locale == null) {
            return Locale.getDefault().getLanguage();
        }

        return _locale;
    }

    /**
     * Set up lacale language
     *
     * @param locale - new locale
     */
    public void setLocale(String locale) {
        _locale = locale;
    }

    /**
     * Detect device location
     *
     * @return true if Gps is used to detect device location
     */
    public boolean isUseGps() {
        return _useGps;
    }

    /**
     * Use GPS to detect device location
     *
     * @param useGps
     */

    public void setUseGps(boolean useGps) {
        if (_useGps) {
            requestLocationUpdates();
        } else {
            stopLocationUpdates();
        }
    }

    /**
     * Retrieve current Location
     *
     * @return current location
     */
    public Location getLocation() {
        return _location;
    }

    /**
     * Set up Location
     *
     * @param location - new location
     */
    public void setLocation(Location location) {
        _location = location;
    }

    Location getDetectedLocation() {
        return _detectedLocation;
    }

    /**
     * For emulator returns string "It's fake token for device emulator".
     *
     * @return device token
     */
    public String getDeviceToken() {
        if ("google_sdk".equals(Build.MODEL) || Build.MODEL.contains("Emulator")) {
            return "It's fake token for device emulator";
        }

        SharedPreferences prefs = _context.getSharedPreferences(SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
        return prefs.getString(DEVICE_TOKEN_PREF, null);
    }

    private void setDeviceToken(String token) {
        Log.i(TAG, "set Device Token; token: " + token);
        SharedPreferences prefs = _context.getSharedPreferences(SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(DEVICE_TOKEN_PREF, token);
        editor.commit();
    }

    /**
     * Request for pending messages
     *
     * @param clientListener If null - used listener that have been set by calling method <b>setCMCClientListener(CMClientListener clientListener)</b>
     * @throws Exception
     */
    public void getPendingMessages(final CMClientListener clientListener) {
        _webClient.getPendingMessages(new WebClientListener() {
            public void response(Object data, Exception error) {
                if (error == null) {
                    addMessages((List<Message>) data);
                }
                if (_messageList.size() > 0) {
                    displayMessages(_messageList, 0);
                }
                sendResponse(clientListener, error);
            }
        });
    }

    void notifyMessageOpened(final Message message) {
        _webClient.notifyMessageOpened(message, null);
    }

    void removeMessage(final Message message) {
        removeMessageById(message.getId());
        _webClient.removeMessage(message, null);
    }

    /**
     * Set up new parameter value
     *
     * @param name           parameter name
     * @param value          new parameter value
     * @param timeout
     * @param clientListener If null - used listener that have been set by calling method <b>setCMCClientListener(CMClientListener clientListener)</b>
     * @throws Exception
     * @throws NoSuchParameterException
     */
    public void setValue(final String name, final String value, int timeout, final CMClientListener clientListener) {
        _webClient.setValue(name, value, timeout, /*null*/new WebClientListener() {

            @Override
            public void response(Object data, Exception error) {
                sendResponse(clientListener, error);
            }
        });
    }

    /**
     * Set up new parameter value
     *
     * @param name           parameter name
     * @param value          new parameter value
     * @param timeout
     * @param clientListener If null - used listener that have been set by calling method <b>setCMCClientListener(CMClientListener clientListener)</b>
     * @throws Exception
     * @throws NoSuchParameterException
     */
    public void setValue(final String name, final double value, int timeout, final CMClientListener clientListener) {
        _webClient.setValue(name, new Double(value), timeout, /*null*/new WebClientListener() {

            @Override
            public void response(Object data, Exception error) {
                sendResponse(clientListener, error);
            }
        });
    }

    /**
     * Set up new parameter value
     *
     * @param name           parameter name
     * @param value          new parameter value
     * @param timeout
     * @param clientListener If null - used listener that have been set by calling method <b>setCMCClientListener(CMClientListener clientListener)</b>
     * @throws Exception
     * @throws NoSuchParameterException
     */
    public void setValue(final String name, final Date value, int timeout, final CMClientListener clientListener) {
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy.MM.dd");
        String val = sdt.format(value);

        _webClient.setValue(name, val, timeout, /*null*/new WebClientListener() {

            @Override
            public void response(Object data, Exception error) {
                sendResponse(clientListener, error);
            }
        });
    }

    /**
     * Set up new parameter value
     *
     * @param name           parameter name
     * @param value          new parameter value
     * @param timeout
     * @param clientListener If null - used listener that have been set by calling method <b>setCMCClientListener(CMClientListener clientListener)</b>
     * @throws Exception
     * @throws NoSuchParameterException
     */
    public void setValue(final String name, final boolean value, int timeout, final CMClientListener clientListener) {
        _webClient.setValue(name, new Boolean(value), timeout, /*null*/new WebClientListener() {

            @Override
            public void response(Object data, Exception error) {
                sendResponse(clientListener, error);
            }
        });
    }

    /**
     * Request to change user id
     *
     * @param newUserId      new user id
     * @param timeout
     * @param clientListener If null - used listener that have been set by calling method <b>setCMCClientListener(CMClientListener clientListener)</b>
     * @throws Exception
     * @throws UserAlreadyExistsException
     */
    public void changeUserId(final String newUserId, int timeout, final CMClientListener clientListener) {
        _webClient.changeUserId(newUserId, timeout, /*null*/new WebClientListener() {

            @Override
            public void response(Object data, Exception error) {
                sendResponse(clientListener, error);
            }
        });
    }

    public void handleIntent(Intent intent) {
        Log.i(TAG, "handle Intent; intent= " + intent);
        if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
            String registrationId = intent.getStringExtra("registration_id");
            if (registrationId != null) {
                Log.i("DEVICE_TOKEN", registrationId);
                setDeviceToken(registrationId);
                registerUser(null);
            }
        } else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
            try {
                final int htmlMessageId = Integer.parseInt(intent.getStringExtra("htmlMessageId"));
                final int messageId = Integer.parseInt(intent.getStringExtra("messageId"));
                final int campaignId = Integer.parseInt(intent.getStringExtra("campaignId"));
                final String notificationText = intent.getStringExtra("message");

                _webClient.getMessageHtml(htmlMessageId, messageId, campaignId, new WebClientListener() {
                    @Override
                    public void response(Object data, Exception error) {
                        if (error == null && data != null && !Utils.isStringEmpty((String) data)) {
                            String html = (String) data;
                            putToNotificationBar(htmlMessageId, messageId, campaignId, notificationText, html);
                        }
                    }
                });
            } catch (Exception ex) {
                Log.e(TAG, "Exception: " + ex);
            }
        } else if (intent.getAction().equals(_context.getPackageName() + ".CLYNG_NOTIFICATION_CLICK")) {
            try {
                int messageId = intent.getIntExtra("messageId", 0);
                int htmlMessageId = intent.getIntExtra("htmlMessageId", 0);
                int campaignId = intent.getIntExtra("campaignId", 0);
                String html = intent.getStringExtra("html");
                Message message = new Message();
                message.setId(messageId);
                message.setMessageId(messageId);
                message.setHtmlMessageId(htmlMessageId);
                message.setIsPush(true);
                message.setHtml(html);
                message.setCampaignId(campaignId);
                ArrayList<Message> messages = new ArrayList<Message>();
                messages.add(message);
                displayMessages(messages, messageId);
            } catch (Throwable t) {
                if (listener != null) {
                    listener.onError(new Exception(t));
                }
            }
        }
    }

    private void putToNotificationBar(int htmlMessageId, int messageId, int campaignId, String message, String html) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) _context.getSystemService(ns);
        Notification notification = new Notification(R.drawable.ic_popup_reminder, message, System.currentTimeMillis());

        Intent intent = new Intent(_context.getPackageName() + ".CLYNG_NOTIFICATION_CLICK");
        intent.putExtra("messageId", messageId);
        intent.putExtra("htmlMessageId", htmlMessageId);
        intent.putExtra("campaignId", campaignId);
        intent.putExtra("html", html);

        PendingIntent contentIntent = PendingIntent.getBroadcast(_context, 0, intent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

        notification.setLatestEventInfo(_context, _appName, message, contentIntent);
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1, notification);
    }

    /**
     * @return "Android"
     */
    public String getPlatform() {
        return "Android";
    }

    /**
     * @return whether device Phone or Tablet
     */
    public String getDeviceType() {

        if (android.os.Build.VERSION.SDK_INT >= 11) { // honeycomb
            Configuration con = _context.getResources().getConfiguration();
            try {
                Method mIsLayoutSizeAtLeast = con.getClass().getMethod("isLayoutSizeAtLeast", int.class);
                Boolean r = (Boolean) mIsLayoutSizeAtLeast.invoke(con, 0x00000004);
                return r ? Tablet : Phone;
            } catch (Exception x) {
                return Phone;
            }
        }
        return Phone;
    }

    private void requestLocationUpdates() {
        for (String provider : _locationManager.getAllProviders()) {
            Location location = _locationManager.getLastKnownLocation(provider);
            if (_detectedLocation == null) {
                _detectedLocation = location;
            } else if (!isLocationOutdated(location)) {
                if (_detectedLocation.getAccuracy() > location.getAccuracy())
                    _detectedLocation = location;
            }

            _locationManager.requestLocationUpdates(provider, 0, 0, _locationListener);
        }

        if (_locationProviderTimer != null) {
            _locationProviderTimer.cancel();
        }
        _locationProviderTimer = new Timer();
        _locationProviderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                _handler.post(new Runnable() {
                    public void run() {
                        stopLocationUpdates();
                    }
                });
            }
        }, 25 * 1000);
    }

    private void stopLocationUpdates() {
        _locationManager.removeUpdates(_locationListener);
        if (_locationProviderTimer != null) {
            _locationProviderTimer.cancel();
            _locationProviderTimer = null;
        }
    }

    private boolean isLocationOutdated(Location location) {
        return location == null ||
                Calendar.getInstance().getTime().getTime() - location.getTime() > 3 * 60 * 1000;
    }

    /*
    private void setUserRegistered(String userId){
        if(userId == null){
            return;
        }
        SharedPreferences prefs = _context.getSharedPreferences(SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("USER_" + userId, true);
        editor.commit();
    }

    private boolean isUserRegistered(String userId){
        if(userId == null){
            return false;
        }

        SharedPreferences prefs = _context.getSharedPreferences(SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("USER_" + userId, false);
    }*/

    private void removeMessageById(int messageId) {
        for (int i = 0; i < _messageList.size(); i++) {
            if (messageId == _messageList.get(i).getId()) {
                _messageList.remove(i);
                return;
            }
        }
    }

    private void addMessages(List<Message> messages) {
        for (Message message : messages) {
            addMessage(message);
        }
    }

    private void addMessage(Message message) {
        if (message.getFilter() == MESSAGE_FILTER || Utils.isStringEmpty(message.getHtml())) {
            return;
        }

        for (Message existingMessage : _messageList) {
            if (existingMessage.getId() == message.getId()) {
                return;
            }
        }
        _messageList.add(message);
    }

    private void displayMessages(ArrayList<Message> messages, int messageId) {
        Log.i(TAG, "displayMessages(); messages= " + messages);
        Intent messageIntent;
        if (_fullScreen) {
            messageIntent = new Intent(_context, FullsreenMessageActivity.class);
        } else {
            messageIntent = new Intent(_context, MessageActivity.class);
        }
        messageIntent.putExtra("MESSAGES", messages);
        messageIntent.putExtra("MESSAGE_ID", messageId);
        messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.i(TAG, "Start Messages Activity(); messageIntent= " + messageIntent);
        _context.startActivity(messageIntent);
    }

    private class LocationListenerImpl implements LocationListener {

        public void onLocationChanged(Location location) {
            if (location != null) {
                _detectedLocation = location;
            }
        }

        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        public void onProviderEnabled(String s) {
        }

        public void onProviderDisabled(String s) {
        }
    }

    void registerActivity(Activity activity) {
        if (_currentActivity != null) {
            _currentActivity.finish();
        }
        _currentActivity = activity;
    }

    void unregisterActivity(Activity activity) {
        if (_currentActivity == activity) {
            _currentActivity = null;
        }
    }
}
