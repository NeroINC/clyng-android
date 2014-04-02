package com.clyng.mobile;

import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ---
 * Date: 5/24/12
 * Time: 11:59
 */
class WebClient extends JSONParserBase {

    private Handler _handler = new Handler();
    private CMClient _client;
    private RestClient _restClient = new RestClient();
    private static final String TAG = "WebClient";

    public WebClient(CMClient client){
        _client = client;
    }

    /**
     * Call server to register user
     * @param listener
     * @throws Exception
     */
    public void registerUser(final WebClientListener listener) {
        String path = _client.getDeviceType().equalsIgnoreCase(CMClient.Phone) ?
                "rulegrid/mobile/device/registerAndroidPhone" : "rulegrid/mobile/device/registerAndroidTablet";
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("apiKey", _client.getApiKey());
//        map.put("appName", _client.getAppName());
        map.put("userId", _client.getUserId());
        map.put("identifier", _client.getDeviceToken());
        map.put("mobileDevicePlatform", _client.getPlatform());

        this.sendRequest(path, map, new InternalListener() {
            public void response(JSONObject data, Exception error) {
                if(error != null){
                    error.printStackTrace();
                }

                invokeResponse(listener, null, error);
            }
        });
    }

    /**
     * Call server to unregister user
     * @param listener
     * @throws Exception
     */
    public void unregisterUser(final WebClientListener listener) {
        String path = "rulegrid/mobile/device/unregisterAndroid";
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("apiKey", _client.getApiKey());
        map.put("userId", _client.getUserId());
        map.put("identifier", _client.getDeviceToken());
        Log.i(TAG, "identifier: " + _client.getDeviceToken());
        this.sendRequest(path, map, new InternalListener() {
            public void response(JSONObject data, Exception error) {
                if(error != null){
                    error.printStackTrace();
                }

                invokeResponse(listener, null, error);
            }
        });
    }

    /**
     * Get pending messages from server
     * @param listener
     * @throws Exception
     */
    public void getPendingMessages(final WebClientListener listener) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("apiKey", _client.getApiKey());
        map.put("userId", _client.getUserId());
        map.put("mobileDevicePlatform", _client.getPlatform());

        this.sendRequest("rulegrid/mobile/message/getMessages", map, new InternalListener() {
            public void response(JSONObject data, Exception error) {
                if(error != null){
                    invokeResponse(listener, null, error);
                    return;
                }

                try{
                    //parse messages
                    List<Message> messages = parseArray(data, "array", new ItemParser<Message>() {
                        public Message parse(Object value) throws JSONException {
                            return parseMessage((JSONObject) value);
                        }
                    });
                    //query for messages html
                /*    for (Message message : messages){
                        message.setHtml(getMessageHtmlSync(message.getId()));
                    }*/
                    //invoke caller
                    invokeResponse(listener, messages, null);
                }catch (Exception ex){
                    invokeResponse(listener, null, ex);
                }
            }
        });
    }

    /**
     * Get html body for message
     * @param htmlMessageId
     * @param messageId
     * @param campaignId
     * @param listener
     */
    public void getMessageHtml(final int htmlMessageId, final int messageId, final int campaignId, final WebClientListener listener) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    invokeResponse(listener, getMessageHtmlSync(htmlMessageId, messageId, campaignId), null);
                }catch (Exception ex){
                    invokeResponse(listener, null, ex);
                }
            }
        });
        thread.start();
    }

    private String getMessageHtmlSync(int htmlMessageId, int messageId, int campaignId) {
        String path = _client.getDeviceType().equalsIgnoreCase(CMClient.Phone) ?
                "rulegrid/mobile/message/getPhoneHTML" : "rulegrid/mobile/message/getTabletHTML";

        Map<String,Object> map = new HashMap<String, Object>();
        map.put("apiKey", _client.getApiKey());
        map.put("userId", _client.getUserId());
        map.put("htmlMessageId", htmlMessageId);
        map.put("campaignId", campaignId);
        map.put("messageId", messageId);

        map.put("mobileDevicePlatform", _client.getPlatform());

        Uri queryUrl = Uri.withAppendedPath(Uri.parse(_client.getServerUrl()), path);
        String httpBody = formatRequestBody(map);

        try{
            return _restClient.put(queryUrl.toString(), httpBody);
        } catch (IOException ex){
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Call server to notify about opened message
     * @param message
     * @param listener
     * @throws Exception
     */
    public void notifyMessageOpened(Message message, final WebClientListener listener) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("apiKey", _client.getApiKey());
        map.put("userId", _client.getUserId());
        map.put("messageId", message.getMessageId());
        map.put("htmlMessageId", message.getHtmlMessageId());

        if( message.isPush() )
            map.put("campaignId", message.getCampaignId());

        map.put("mobileDevicePlatform", _client.getPlatform());

        this.sendRequest("rulegrid/mobile/message/messageOpened", map, new InternalListener() {
            public void response(JSONObject data, Exception error) {
                invokeResponse(listener, null, error);
            }
        });
    }

    /**
     * Call server to set value
     * @param name
     * @param value
     * @param timeout
     * @param listener
     * @throws Exception
     */
    public void setValue( final String name, final Object value, final int timeout, final WebClientListener listener ) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("apiKey", _client.getApiKey());
        map.put("userId", _client.getUserId());
        map.put("name", name);
        map.put("value", value);

        InternalListener l = new InternalListener() {
            public void response(JSONObject data, Exception error) {

                if( listener != null )
                    invokeResponse(listener, null, error);
                else if( timeout != 0 )
                {
                    if( error != null && error.getClass() == NoSuchUserException.class )
                    {
                        throw (NoSuchUserException)error;
                    }else if( error != null && error.getClass() == NoSuchParameterException.class )
                    {
                        throw (NoSuchParameterException)error;
                    }
                }
            }
        };

        if( timeout != 0 )
        {
            _restClient.setTimeout( timeout );
            this.sendRequestSync("rulegrid/api/userParams/setValue", map, l);
            _restClient.setTimeout( 0 );
        } else
        {
            this.sendRequest("rulegrid/api/userParams/setValue", map, l);
        }
    }

    /**
     * Call server to change user id
     * @param newUserId
     * @param timeout
     * @param listener
     * @throws Exception
     */
    public void changeUserId(final String newUserId, final int timeout, final WebClientListener listener) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("apiKey", _client.getApiKey());
        map.put("userId", _client.getUserId());
        map.put("newUserId", newUserId);

        InternalListener l = new InternalListener() {
            public void response(JSONObject data, Exception error) {
                if( listener != null )
                    invokeResponse(listener, null, error);
                else if( timeout != 0 )
                {
                    if( error != null && error.getClass() == NoSuchUserException.class )
                    {
                        throw (NoSuchUserException)error;
                    }else if( error != null && error.getClass() == UserAlreadyExistsException.class )
                    {
                        throw (UserAlreadyExistsException)error;
                    }
                }
            }
        };
        if( timeout != 0 )
        {
            _restClient.setTimeout(timeout);
            this.sendRequestSync("rulegrid/api/user/changeUserId", map, l);
            _restClient.setTimeout(0);
        }else
            this.sendRequest("rulegrid/api/user/changeUserId", map, l);
    }

    /**
     * Call server to remove message
     * @param message
     * @param listener
     * @throws Exception
     */
    public void removeMessage( final Message message, final WebClientListener listener) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("apiKey", _client.getApiKey());
        map.put("userId", _client.getUserId());
        map.put( message.isPush() ? "htmlMessageId" : "messageId", message.isPush() ? message.getHtmlMessageId() : message.getMessageId() );
        map.put("mobileDevicePlatform", _client.getPlatform());

        this.sendRequest("rulegrid/mobile/message/removeMessage", map, new InternalListener() {
            public void response(JSONObject data, Exception error) {
                invokeResponse(listener, null, error);
            }
        });
    }

    /**
     * Call server to send event
     * @param event Event name
     * @param params
     * @param listener
     * @throws Exception
     */
    public void sendEvent(String event, Map<String,Object> params, final WebClientListener listener) {
        Map<String,Object> map = params != null ? new HashMap<String, Object>(params) : new HashMap<String, Object>();
        map.put("eventName", event);
        map.put("apiKey", _client.getApiKey());
        map.put("userId", _client.getUserId());
        // #423 fixed
        if( _client.getEmail() != null && _client.getEmail().length() != 0 )
            map.put("email", _client.getEmail());
        map.put("locale", _client.getLocale());
        map.put("mobileDeviceToken", _client.getDeviceToken());
        map.put("mobileDevicePlatform", _client.getPlatform());
        map.put("mobileDeviceType", _client.getDeviceType());
        Location location = _client.isUseGps() ? _client.getDetectedLocation() : _client.getLocation();
        if(location != null){
            map.put("latitude", location.getLatitude());
            map.put("longitude", location.getLongitude());
        }

        this.sendRequest("rulegrid/events/process", map, new InternalListener() {
            public void response(JSONObject data, Exception error) {
                invokeResponse(listener, null, error);
            }
        });
    }


    private void sendRequestSync(String httpPath, Map<String,Object> params, final InternalListener listener) {
        final Uri queryUrl = Uri.withAppendedPath(Uri.parse(_client.getServerUrl()), httpPath);
        final String httpBody = formatRequestBody(params);

        Log.i("ClyngLib", queryUrl.toString());
        Log.i("ClyngLib", httpBody);

        try{
            String response = _restClient.put(queryUrl.toString(), httpBody);
            Log.i("ClyngLib", "response: " + response);
            if(response != null && !response.equals("")){
                response = response.trim();
                if(response.startsWith("[")){
                    JSONObject object = new JSONObject();
                    object.put("array", new JSONArray(response));
                    listener.response(object, null);
                } else {
                    JSONObject responseObj = new JSONObject(response);
                    String status = getString(responseObj, "status");
                    if( status != null && status.compareTo( "ERROR" ) == 0 )
                    {
                        String message = getString(responseObj, "error_message");
                        if(message == null){
                            message = getString(responseObj, "message");
                        }

                        if( message == null )
                            message = "";

                        String code = getString( responseObj, "code" );
                        if( code.compareToIgnoreCase("NO.SUCH.USER") == 0 )
                        {
                            throw new NoSuchUserException(message);
                        }

                        if( code.compareToIgnoreCase("NO.SUCH.PARAMETER") == 0 )
                        {
                            throw new NoSuchParameterException(message);
                        }

                        if( code.compareToIgnoreCase("ERROR.USER.ALREADY.EXISTS") == 0 )
                        {
                            throw new UserAlreadyExistsException(message);
                        }

                        // default handler
                        throw new IllegalArgumentException(message);
                    }


                    listener.response(new JSONObject(response), null);
                }
            } else {
                listener.response(null, null);
            }
        }catch (Exception ex){
            listener.response(null, ex);
        }
    }


    private void sendRequest(final String httpPath, final Map<String,Object> params, final InternalListener listener){
        /*final Uri queryUrl = Uri.withAppendedPath(Uri.parse(_client.getServerUrl()), httpPath);
        final String httpBody = formatRequestBody(params);

        Log.i("ClyngLib", queryUrl.toString());
        Log.i("ClyngLib", httpBody);*/

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try{
                    WebClient.this.sendRequestSync( httpPath, params, listener );
        /*            String response = _restClient.put(queryUrl.toString(), httpBody);
                    if(response != null && !response.equals("")){
                        response = response.trim();
                        if(response.startsWith("[")){
                            JSONObject object = new JSONObject();
                            object.put("array", new JSONArray(response));
                            listener.response(object, null);
                        } else {
                            JSONObject responseObj = new JSONObject(response);
                            String status = getString(responseObj, "status");
                            if( status != null && status.compareTo( "ERROR" ) == 0 )
                            {
                                String message = getString(responseObj, "error_message");
                                if(message == null){
                                    message = getString(responseObj, "message");
                                }

                                if( message == null )
                                    message = "";

                                // default handler
                                throw new IllegalArgumentException(message);
                            }


                            listener.response(new JSONObject(response), null);
                        }
                    } else {
                        listener.response(null, null);
                    }   */
                }catch (Exception ex){
                    listener.response(null, ex);
                }
            }
        });
        thread.start();
    }

    private String formatRequestBody(Map<String,Object> params){
        JSONObject body = new JSONObject();
        if(params == null){
            return body.toString();
        }

        for(String key : params.keySet()){
            Object value = params.get(key);
            if(value != null){
                try {
                    body.put(key, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return body.toString();
    }

    private void invokeResponse(final WebClientListener listener, final Object data, final Exception error){
        if(listener == null){
            return;
        }

        _handler.post(new Runnable() {
            public void run() {
                listener.response(data, error);
            }
        });
    }

    private Message parseMessage(JSONObject object) throws JSONException{
        Message message = new Message();
        message.setCustomerId(getInt(object, "customer_id"));
        message.setDisplayWidth(getInt(object, "display_w"));
        message.setDisplayHeight(getInt(object, "display_h"));
        message.setName(getString(object, "name"));
        message.setUnique(getBoolean(object, "unique"));
        message.setId(getInt(object, "id"));
        message.setExpiration(getInt(object, "expiration"));
        message.setFilter(getInt(object, "filter"));
        message.setIsPhone(getBoolean(object, "isPhone"));
        message.setIsTablet(getBoolean(object, "isTablet"));
        message.setEmbTag(new EmbeddedElement());
        message.setMessageId( getInt(object, "messageId") );
        message.setHtmlMessageId( getInt(object, "htmlMessageId") );

        JSONObject htmlMessageObject = getObject( object, "htmlMessage" );

      //  Message.HtmlMessage htmlMessage = new Message.HtmlMessage();
        //htmlMessage.setPhoneHtml( getString(htmlMessageObject, "phoneHtml") );
        //htmlMessage.setTabletHtml( getString(htmlMessageObject, "tabletHtml") );
        //message.setHtmlMessage(htmlMessage);

        if( _client.getDeviceType().equalsIgnoreCase(CMClient.Phone) )
        {
          //  message.setHtml( htmlMessage.getPhoneHtml() );
            message.setHtml( getString(htmlMessageObject, "phoneHtml") );
        }else
        {
            if( message.getIsTablet() )
                //message.setHtml(htmlMessage.getTabletHtml());
                message.setHtml( getString(htmlMessageObject, "tabletHtml") );
            else if( message.getIsPhone() )
                //message.setHtml( htmlMessage.getPhoneHtml());
                message.setHtml( getString(htmlMessageObject, "phoneHtml") );

        }

        String embedString = object.getString("embed_tag");
        if(embedString != null){
            embedString = embedString.trim();
            if(embedString.startsWith("'")){
                embedString = embedString.substring(1);
            }
            if(embedString.endsWith("'")){
                embedString = embedString.substring(0, embedString.length() - 1);
            }

            object = new JSONObject(embedString);
            String tagName = _client.getDeviceType().equalsIgnoreCase(CMClient.Phone) ? "phoneHtml" : "tabletHtml";
            JSONObject tag = object.getJSONObject(tagName);

            if(tag != null){
                message.getEmbTag().setDisplay(getString(tag, "disptype"));
                message.getEmbTag().setRemoveAct(getString(tag, "removeact"));
                message.getEmbTag().setClickClose(getBoolean(tag, "clickClose"));
                message.getEmbTag().setEmbeddedTag(getString(tag, "embedtag"));
                if(tag.has("dims") && !tag.isNull("dims")){
                    message.getEmbTag().setWidth(getInt(getObject(tag, "dims"), "width"));
                    message.getEmbTag().setHeight(getInt(getObject(tag, "dims"), "height"));
                }
            }
        }

        return message;
    }

    interface InternalListener {
        void response(JSONObject data, Exception error);
    }
}
