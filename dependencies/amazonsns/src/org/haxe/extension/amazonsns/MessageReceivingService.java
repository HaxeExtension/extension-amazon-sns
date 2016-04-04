package org.haxe.extension.amazonsns;

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.content.ComponentName;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/*
 * This service is designed to run in the background and receive messages from gcm. If the app is in the foreground
 * when a message is received, it will immediately be posted. If the app is not in the foreground, the message will be saved
 * and a notification is posted to the NotificationManager.
 */
public class MessageReceivingService extends Service{
    private GoogleCloudMessaging gcm;
    public static SharedPreferences savedValues;
    private static String packageName = null;
    private static int notificationIconID = 0;
    private static int multiNotificationIconID = 0;

    private static Intent createIntent(){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName,packageName+".MainActivity"));
        return intent;
    }

    public static void sendToApp(Bundle extras, Context context){
        if(AmazonSNS.mainActivity == null) {
            Log.i(AmazonSNS.LOG_PREFIX+"MessageReceivingService.sendToApp", "I won't do anything since mainActivity is not alive anymore.");
            return;
        }
        Intent intent = createIntent();
        intent.putExtras(extras);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void onCreate(){
        super.onCreate();
        Log.i(AmazonSNS.LOG_PREFIX+"MessageReceivingService.onCreate", "Begins...");
        packageName = getString(R.string.app_package);        
        final String preferencesLabel = getString(R.string.preferences);
        savedValues = getBaseContext().getSharedPreferences(preferencesLabel, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);

        gcm = GoogleCloudMessaging.getInstance(getBaseContext());
        SharedPreferences savedValues = PreferenceManager.getDefaultSharedPreferences(this);
        if(savedValues.getBoolean(getString(R.string.first_launch), true)){
            register();
            SharedPreferences.Editor editor = savedValues.edit();
            editor.putBoolean(getString(R.string.first_launch), false);
            editor.commit();
        }
        // Let AmazonSNS know we have just initialized and there may be stored messages
        sendToApp(new Bundle(), this);
    }

    protected static void saveToLog(Bundle extras, Context context){
        String numOfMissedMessagesLabel = context.getString(R.string.num_of_missed_messages);
        String linesOfMessageLabel = context.getString(R.string.lines_of_message_count);
        int numOfMissedMessages = savedValues.getInt(numOfMissedMessagesLabel, 0) + 1;
        int linesOfMessageCount = savedValues.getInt(linesOfMessageLabel, 0);
        Log.i(AmazonSNS.LOG_PREFIX, "Saving missed message number " + numOfMissedMessages);

        SharedPreferences.Editor editor = savedValues.edit();
        for(String key : extras.keySet()){
            String line = String.format("%s=%s", key, extras.getString(key));
            editor.putString("MessageLine" + linesOfMessageCount, line);
            Log.i(AmazonSNS.LOG_PREFIX, "Saving String: " + "MessageLine" + linesOfMessageCount + " = " + line);
            linesOfMessageCount++;
        }

        editor.putInt(linesOfMessageLabel, linesOfMessageCount);
        editor.putInt(numOfMissedMessagesLabel, numOfMissedMessages);
        editor.commit();
        Intent intent = createIntent();
        postNotification(intent, context, numOfMissedMessages);
    }

    private static void loadNotificationIcons(Context context){
        if(notificationIconID != 0) return;
 
        notificationIconID = context.getResources().getIdentifier("notification-icon", "drawable", packageName);
        multiNotificationIconID = context.getResources().getIdentifier("multi-notification-icon", "drawable", packageName);

        if(notificationIconID == 0) notificationIconID = context.getResources().getIdentifier("icon", "drawable", packageName);
        if(multiNotificationIconID == 0) multiNotificationIconID = notificationIconID;        
    }

    protected static void postNotification(Intent intentAction, Context context, int numOfMissedMessages) {
        loadNotificationIcons(context);

        String messageKey = context.getString(numOfMissedMessages>1?R.string.multiple_notifications_msg:R.string.single_notification_msg);
        String titleKey = context.getString(numOfMissedMessages>1?R.string.multiple_notifications_title:R.string.single_notification_title);

        final String message = savedValues.getString(messageKey,messageKey).replace("%%",""+numOfMissedMessages);
        final String title = savedValues.getString(titleKey,context.getString(R.string.app_title)).replace("%%",""+numOfMissedMessages);

        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentAction, Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL);
        final Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(numOfMissedMessages>1?multiNotificationIconID:notificationIconID)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .getNotification();

        mNotificationManager.notify(R.string.notification_number, notification);
    }

    private void register() {
        new AsyncTask(){
            protected Object doInBackground(final Object... params) {
                String token;
                try {
                    token = gcm.register(AmazonSNS.senderID);
                    Log.i(AmazonSNS.LOG_PREFIX, "Registration ID = " + token);
                } 
                catch (IOException e) {
                    Log.i(AmazonSNS.LOG_PREFIX, "Registration Error: " + e.getMessage());
                }
                return true;
            }
        }.execute(null, null, null);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

}