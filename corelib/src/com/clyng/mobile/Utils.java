package com.clyng.mobile;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: ---
 * Date: 5/22/12
 * Time: 14:57
 */
class Utils {

    static Boolean getBoolean(Properties properties, String key, Boolean defaultValue){
        String value = properties.getProperty(key);
        if(value == null){
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    static boolean isStringEmpty(String value){
        return value == null || value.trim().equals("");
    }

}
