package com.clyng.demo;

import android.content.SharedPreferences;

/**
 * Created by IntelliJ IDEA.
 *
 * @author alximik
 * @since 08.08.12 16:12
 */
public class AppSettings {

    private static String PREFS_NAME = "clyng_settings";

    private String serverUrl;
    private String userId;
    private String email;
    private boolean fullscreen;
    private String customerKey;

    private static class FieldNames {
        private static String serverUrl = "server_url";

        private static String userId = "user_id";
        private static String email = "email";
        private static String fullscreen = "fullscreen";
        private static String customerKey = "customer_key";
    }

    public static  AppSettings load() {
        AppSettings appSettings = new AppSettings();

        SharedPreferences settings = DemoApplication.getContext().getSharedPreferences(PREFS_NAME, 0);

        appSettings.customerKey = settings.getString(FieldNames.customerKey, null);
        appSettings.email = settings.getString(FieldNames.email, null);
        appSettings.fullscreen = settings.getBoolean(FieldNames.fullscreen, false);
        appSettings.serverUrl = settings.getString(FieldNames.serverUrl, null);
        appSettings.userId = settings.getString(FieldNames.userId, null);

        return appSettings;
    }

    public void save() {
        SharedPreferences storage = DemoApplication.getContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = storage.edit();
        editor.putBoolean(FieldNames.fullscreen, fullscreen);
        editor.putString(FieldNames.serverUrl, serverUrl);
        editor.putString(FieldNames.userId, userId);
        editor.putString(FieldNames.email, email);
        editor.putString(FieldNames.customerKey, customerKey);
        editor.commit();
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }
}
