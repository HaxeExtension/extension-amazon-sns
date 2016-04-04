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

public class AmazonSNS extends Extension {

    /////// HAXE STUFF ///////
    public static String senderID = null;
    private static HaxeObject callback = null;

    private static String singleNotificationTitle = null;
    private static String multipleNotificationTitle = null;
    private static String singleNotificationMessage = null;
    private static String multipleNotificationMessage = null;

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

    private static void onMessage(String s){
        callback.call1("_onMessage",s);
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

        String message = "";
        String linesOfMessageLabel = mainActivity.getString(R.string.lines_of_message_count);
        String numOfMissedMessagesLabel = mainActivity.getString(R.string.num_of_missed_messages);
        int numOfMissedMessages = MessageReceivingService.savedValues.getInt(numOfMissedMessagesLabel, 0);
        int linesOfMessageCount = MessageReceivingService.savedValues.getInt(linesOfMessageLabel, 0);

        Log.i(LOG_PREFIX+"getMessages","missed " + numOfMissedMessages + " message(s) / "+linesOfMessageCount+" line(s)");

        if(numOfMissedMessages+linesOfMessageCount > 0){

            NotificationManager mNotification = (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotification.cancel(R.string.notification_number);

            SharedPreferences.Editor editor = MessageReceivingService.savedValues.edit();
            for(int i = 0; i < linesOfMessageCount; i++){
                String key = "MessageLine"+i;
                message+= MessageReceivingService.savedValues.getString(key, "") + "\n";
                editor.remove(key);
            }
            editor.putInt(numOfMissedMessagesLabel, 0);
            editor.putInt(linesOfMessageLabel, 0);
            editor.commit();

        } else {
            
            Log.i(LOG_PREFIX+"getMessages","Seeking for intent messages...");
            Intent intent = mainActivity.getIntent();
            if(intent!=null){
                Bundle extras = intent.getExtras();
                if(extras!=null){
                    for(String key: extras.keySet()){
                        message+= key + "=" + extras.getString(key) + "\n";
                    }
                }
            }

        }

        if(message!=""){
            onMessage(message);
            Log.i(LOG_PREFIX+"getMessages","Message: "+message);
        }else{
            Log.i(LOG_PREFIX+"getMessages","No messages");
        }
    }
}
