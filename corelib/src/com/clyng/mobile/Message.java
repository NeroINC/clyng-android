package com.clyng.mobile;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: ---
 * Date: 5/24/12
 * Time: 11:51
 */
class Message implements Serializable {


   /* public static class HtmlMessage  implements Serializable
    {
        private int _customerId;
        private String _html;
        private String _phoneHtml;
        private int _pendingAdId;
        private String _tabletHtml;

        public String getPhoneHtml() {
            return _phoneHtml;
        }

        public void setPhoneHtml(String phoneHtml) {
            this._phoneHtml = phoneHtml;
        }

        public String getTabletHtml() {
            return _tabletHtml;
        }

        public void setTabletHtml(String tabletHtml) {
            this._tabletHtml = tabletHtml;
        }
    }*/

    private int _customerId;
    private int _displayWidth;
    private int _displayHeight;
    private String _name;
    private boolean _unique;
    private int _expiration;
    private int _filter;
    private int _id;
    private EmbeddedElement _embTag;
    private String _html;
    private boolean _viewed;
    private boolean _isPhone;
    private boolean _isTablet;
    private int _messageId;
    private int _htmlMessageId;
    private boolean _isPush;
    private int _campaignId;

  //  private HtmlMessage _htmlMessage;

    public int getCustomerId() {
        return _customerId;
    }

    public void setCustomerId(int customerId) {
        _customerId = customerId;
    }

    public int getDisplayWidth() {
        return _displayWidth;
    }

    public void setDisplayWidth(int displayWidth) {
        _displayWidth = displayWidth;
    }

    public int getDisplayHeight() {
        return _displayHeight;
    }

    public void setDisplayHeight(int displayHeight) {
        _displayHeight = displayHeight;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public boolean isUnique() {
        return _unique;
    }

    public void setUnique(boolean unique) {
        _unique = unique;
    }

    public int getExpiration() {
        return _expiration;
    }

    public void setExpiration(int expiration) {
        _expiration = expiration;
    }

    public int getFilter() {
        return _filter;
    }

    public void setFilter(int filter) {
        _filter = filter;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public EmbeddedElement getEmbTag() {
        return _embTag;
    }

    public void setEmbTag(EmbeddedElement embTag) {
        _embTag = embTag;
    }

    public String getHtml() {
        return _html;
    }

    public void setHtml(String html) {
        _html = html;
    }

    public boolean isAutoRemove(){
        String removeAct = getEmbTag().getRemoveAct();
        return removeAct != null && removeAct.equalsIgnoreCase("auto");
    }

    public boolean isViewed() {
        return _viewed;
    }

    public void setViewed(boolean viewed) {
        _viewed = viewed;
    }

    public boolean getIsPhone() {
        return _isPhone;
    }

    public void setIsPhone(boolean _isPhone) {
        this._isPhone = _isPhone;
    }

   /* public HtmlMessage getHtmlMessage() {
        return _htmlMessage;
    }

    public void setHtmlMessage(HtmlMessage _htmlMessage) {
        this._htmlMessage = _htmlMessage;
    }*/

    public boolean getIsTablet() {
        return _isTablet;
    }

    public void setIsTablet(boolean _isTablet) {
        this._isTablet = _isTablet;
    }


    public int getMessageId() {
        return _messageId;
    }

    public void setMessageId(int _messageId) {
        this._messageId = _messageId;
    }

    public int getHtmlMessageId() {
        return _htmlMessageId;
    }

    public void setHtmlMessageId(int _htmlMessageId) {
        this._htmlMessageId = _htmlMessageId;
    }

    public boolean isPush() {
        return _isPush;
    }

    public void setIsPush(boolean _isPush) {
        this._isPush = _isPush;
    }

    public int getCampaignId() {
        return _campaignId;
    }

    public void setCampaignId(int _campaignId) {
        this._campaignId = _campaignId;
    }
}
