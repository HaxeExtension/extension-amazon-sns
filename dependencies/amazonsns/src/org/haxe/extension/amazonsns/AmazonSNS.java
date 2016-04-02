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

public class AmazonSNS extends Extension {
    private SharedPreferences savedValues;
    private String numOfMissedMessages;

    // Since this activity is SingleTop, there can only ever be one instance. This variable corresponds to this instance.
    public static Boolean inBackground = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        numOfMissedMessages = mainActivity.getString(R.string.num_of_missed_messages);
        //setContentView(R.layout.activity_main);
        //tView = (TextView) findViewById(R.id.tViewId);
        //tView.setMovementMethod(new ScrollingMovementMethod());
        mainActivity.startService(new Intent(mainActivity, MessageReceivingService.class));
    }

    public void onStop(){
        super.onStop();
        inBackground = true;
    }

    public void onRestart(){
        super.onRestart();
        //tView.setText("");;
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
            Log.i("displaying message", newMessage);
            //tView.append(newMessage);
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
            Log.i("onResume","missed " + numOfMissedMessages + " message" + plural);
            //tView.append("You missed " + numOfMissedMessages +" message" + plural + ". Your most recent was:\n");
            for(int i = 0; i < savedValues.getInt(linesOfMessageCount, 0); i++){
                String line = savedValues.getString("MessageLine"+i, "");
                message+= (line + "\n");
            }
            NotificationManager mNotification = (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotification.cancel(R.string.notification_number);
            SharedPreferences.Editor editor=savedValues.edit();
            editor.putInt(this.numOfMissedMessages, 0);
            editor.putInt(linesOfMessageCount, 0);
            editor.commit();
        }
        else{
            Log.i("onResume","no missed messages");
            Intent intent = mainActivity.getIntent();
            Log.i("onResume","no missed messages!!!!");
            if(intent!=null){
                Bundle extras = intent.getExtras();
                if(extras!=null){
                    for(String key: extras.keySet()){
                        message+= key + "=" + extras.getString(key) + "\n";
                    }
                }
            }
        }
            Log.i("onResume","DONE!");
        message+="\n";
        return message;
    }
}
