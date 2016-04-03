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

    public static void init(String senderID, HaxeObject callback) {
    	if (AmazonSNS.senderID != null) return;
    	AmazonSNS.senderID = senderID;
    	AmazonSNS.callback = callback;

		mainActivity.runOnUiThread(new Runnable() {
			public void run() {    	
				numOfMissedMessages = mainActivity.getString(R.string.num_of_missed_messages);
				mainActivity.startService(new Intent(mainActivity, MessageReceivingService.class));
    		}
    	});
    }

    /////// AMAZON STUFF ///////
    public static final String LOG_PREFIX = "AmazonSNS-Extension: ";

    private SharedPreferences savedValues;
    private static String numOfMissedMessages;
    public static Boolean inBackground = true;

    public static void onMessage(String s){
    	if(callback==null) return;
    	callback.call1("onMessage",s);
    }

    public void onStop(){
        super.onStop();
        inBackground = true;
    }

    public void onResume(){
        super.onResume();
        inBackground = false;
        savedValues = MessageReceivingService.savedValues;
        int numOfMissedMessages = 0;
        if(savedValues != null){
            numOfMissedMessages = savedValues.getInt(this.numOfMissedMessages, 0);
        }
        String newMessage = getMessage(numOfMissedMessages);
        if(newMessage!=""){
            onMessage(newMessage);
            Log.i(LOG_PREFIX+"displaying message", newMessage);
        }
    }

    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        mainActivity.setIntent(intent);
    }

    // If messages have been missed, check the backlog. Otherwise check the current intent for a new message.
    private String getMessage(int numOfMissedMessages) {
        String message = "";
        String linesOfMessageCount = mainActivity.getString(R.string.lines_of_message_count);
        if(numOfMissedMessages > 0){
            String plural = numOfMissedMessages > 1 ? "s" : "";
            Log.i(LOG_PREFIX+"onResume","missed " + numOfMissedMessages + " message" + plural);
            for(int i = 0; i < savedValues.getInt(linesOfMessageCount, 0); i++){
                String line = savedValues.getString("MessageLine"+i, "");
                message+= (line + "\n");
            }
            Log.i(LOG_PREFIX+"onResume","Message: "+message);
            NotificationManager mNotification = (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotification.cancel(R.string.notification_number);
            SharedPreferences.Editor editor=savedValues.edit();
            editor.putInt(this.numOfMissedMessages, 0);
            editor.putInt(linesOfMessageCount, 0);
            editor.commit();
        } else {
            Log.i(LOG_PREFIX+"onResume","no missed messages");
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
        Log.i(LOG_PREFIX+"onResume","DONE!");
        return message;
    }
}
