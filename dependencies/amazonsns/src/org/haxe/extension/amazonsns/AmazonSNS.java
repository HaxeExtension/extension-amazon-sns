package org.haxe.extension.amazonsns;
import java.util.Date;
import java.util.Queue;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import org.haxe.extension.Extension;
import org.haxe.lime.HaxeObject;
import org.json.*;

public class AmazonSNS extends Extension {

    /////// HAXE STUFF ///////
    public static String senderID = null;
    private static HaxeObject callback = null;

    private static String singleNotificationTitle = null;
    private static String multipleNotificationTitle = null;
    private static String singleNotificationMessage = null;
    private static String multipleNotificationMessage = null;
    public static boolean notifiedRegistrationSutatus = false;

    public static void init(String senderID, HaxeObject callback) {
    	if (AmazonSNS.senderID != null) return;
    	AmazonSNS.senderID = senderID;
    	AmazonSNS.callback = callback;
        mainActivity.startService(new Intent(mainActivity, MessageReceivingService.class));
        getMessages();
    }

    public static boolean setNotificationTitles(String single, String multiple) {
        if(single == null || multiple == null) return false;
        singleNotificationTitle = null;
        multipleNotificationTitle = null;

        if(MessageReceivingService.savedValues == null){
            singleNotificationTitle = single;
            multipleNotificationTitle = multiple;
            return true;
        }
        SharedPreferences.Editor editor = MessageReceivingService.savedValues.edit();
        editor.putString(mainContext.getString(R.string.single_notification_title),single);
        editor.putString(mainContext.getString(R.string.multiple_notifications_title),multiple);
        editor.commit();
        return true;
    }

    public static boolean setNotificationMessages(String single, String multiple) {
        if(single == null || multiple == null) return false;
        singleNotificationMessage = null;
        multipleNotificationMessage = null;

        if(MessageReceivingService.savedValues == null){
            singleNotificationMessage = single;
            multipleNotificationMessage = multiple;
            return false;
        }
        SharedPreferences.Editor editor = MessageReceivingService.savedValues.edit();
        editor.putString(mainContext.getString(R.string.single_notification_msg),single);
        editor.putString(mainContext.getString(R.string.multiple_notifications_msg),multiple);
        editor.commit();
        return true;
    }

    public static void registerRetry(){
        MessageReceivingService.register();
    }

    public static String getRegistrationId() {
        if(MessageReceivingService.savedValues == null) return null;
        return MessageReceivingService.savedValues.getString(mainContext.getString(R.string.registration_id),"");
    }

    public static String getRegistrationError() {
        if(MessageReceivingService.savedValues == null) return null;
        return MessageReceivingService.savedValues.getString(mainContext.getString(R.string.registration_error),"");
    }

    private static void notifyRegistrationStatus(final String registration_id){
        if(notifiedRegistrationSutatus==true) return;
        if(callback==null) return;
        if(MessageReceivingService.savedValues == null) return;

        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                String token = registration_id;
                if(token == null || token.equals("")) token = MessageReceivingService.savedValues.getString(mainContext.getString(R.string.registration_id),"");
                if(!token.equals("")) {
                    notifiedRegistrationSutatus = true;
                    callback.call1("_onRegistrationSuccess",token);
                    return;
                }
                String error = MessageReceivingService.savedValues.getString(mainContext.getString(R.string.registration_error),"");
                if(!error.equals("")) {
                    notifiedRegistrationSutatus = true;
                    callback.call1("_onRegistrationError",error);
                }
            }
        }); 
    }

    private static void onNotificationsReceived(final String json){
        if(callback==null) return;
        mainActivity.runOnUiThread(new Runnable() {
            public void run() { 
                callback.call1("_onNotificationsReceived",json);
            }
        });
    }

    private static void submitNotificationTexts(){
        setNotificationTitles(singleNotificationTitle, multipleNotificationTitle);
        setNotificationMessages(singleNotificationMessage, multipleNotificationMessage);
    }

    /////// AMAZON STUFF ///////
    public static final String LOG_PREFIX = "AmazonSNS-Extension: ";
    public static Boolean inBackground = true;

    public void onStop(){
        inBackground = true;
    }

    public void onResume(){
        inBackground = false;
        submitNotificationTexts();
        getMessages();
    }

    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        mainActivity.setIntent(intent);
    }

    // If messages have been missed, check the backlog. Otherwise check the current intent for a new message.
    private static void getMessages() {

        if(callback==null) {
            Log.i(LOG_PREFIX+"getMessages", "Haxe callback is NULL! (aborting)");
            return;
        }
        if(MessageReceivingService.savedValues==null) {
            Log.i(LOG_PREFIX+"getMessages", "MessageReceivingService.savedValues is NULL! (aborting)");
            return;
        }

        String messages = "";

        String numOfMissedMessagesLabel = mainActivity.getString(R.string.num_of_missed_messages);
        int numOfMissedMessages = MessageReceivingService.savedValues.getInt(numOfMissedMessagesLabel, 0);

        Log.i(LOG_PREFIX+"getMessages","missed " + numOfMissedMessages + " message(s)");

        if(numOfMissedMessages > 0){

            NotificationManager mNotification = (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotification.cancel(R.string.notification_number);

            SharedPreferences.Editor editor = MessageReceivingService.savedValues.edit();
            for(int i = 1; i <=numOfMissedMessages; i++){
                String key = "MessageLine"+i;
                String line = MessageReceivingService.savedValues.getString(key, "");
                editor.remove(key);
                if(line.equals("")) continue;
                if(!messages.equals("")) messages+=",";
                messages+=line;
            }
            editor.putInt(numOfMissedMessagesLabel, 0);
            editor.commit();

        } 

        Log.i(LOG_PREFIX+"getMessages","Seeking for intent messages...");
        Intent intent = mainActivity.getIntent();
        if(intent!=null){
            Bundle extras = intent.getExtras();
            String line = extrasToJson(extras, true);
            if(!line.equals("")){
                if(!messages.equals("")) messages+=",";
                messages += line;
            }
        }

        notifyRegistrationStatus(null);
        if(!messages.equals("")){
            String json = "["+messages+"]";
            onNotificationsReceived(json);
            Log.i(LOG_PREFIX+"getMessages","Message: "+json);
        }else{
            Log.i(LOG_PREFIX+"getMessages","No messages");
        }
    }

    public static String extrasToJson(Bundle extras, boolean justArrived){
        if(extras==null) return "";
        try{
            if(extras.containsKey("registration_id") && extras.size()==1) {
                notifyRegistrationStatus(extras.getString("registration_id"));
                return "";
            }
            if(!extras.containsKey("collapse_key")) return "";
            JSONObject m = new JSONObject();
            for(String key: extras.keySet()) m.put(key, extras.getString(key));
            m.put("justArrived",(justArrived)?"true":"false");
            return m.toString();
        }catch(JSONException j){
            Log.i(LOG_PREFIX+"getMessages","JSON Exception: "+j.getMessage());
        }
        return "";
    }
}
