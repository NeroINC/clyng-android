package com.clyng.mobile;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: ---
 * Date: 5/24/12
 * Time: 11:54
 */
class EmbeddedElement implements Serializable {

    private String _display;
    private String _removeAct;
    private int _width;
    private int _height;
    private boolean _clickClose;
    private String _embeddedTag;

    public String getDisplay() {
        return _display;
    }

    public void setDisplay(String display) {
        _display = display;
    }

    public String getRemoveAct() {
        return _removeAct;
    }

    public void setRemoveAct(String removeAct) {
        _removeAct = removeAct;
    }

    public int getWidth() {
        return _width;
    }

    public void setWidth(int width) {
        _width = width;
    }

    public int getHeight() {
        return _height;
    }

    public void setHeight(int height) {
        _height = height;
    }

    public boolean isClickClose() {
        return _clickClose;
    }

    public void setClickClose(boolean clickClose) {
        _clickClose = clickClose;
    }

    public String getEmbeddedTag() {
        return _embeddedTag;
    }

    public void setEmbeddedTag(String embeddedTag) {
        _embeddedTag = embeddedTag;
    }
}
