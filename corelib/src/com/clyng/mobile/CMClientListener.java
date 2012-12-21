package com.clyng.mobile;

/**
 * Created with IntelliJ IDEA.
 * User: ---
 * Date: 30.11.12
 * Time: 11:36
 * To change this template use File | Settings | File Templates.
 */
public interface CMClientListener {
    void onSuccess();
    void onError(Exception e);
}
