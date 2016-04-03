package org.haxe.extension.amazonsns;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ExternalReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if(intent!=null){
            Bundle extras = intent.getExtras();
            if(!AmazonSNS.inBackground){
                MessageReceivingService.sendToApp(extras, context);
            } else {
                MessageReceivingService.saveToLog(extras, context);
            }
        }
    }
}

