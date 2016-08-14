package com.firefighter.skynetconfirmed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by baynhuchim on 8/14/16.
 */
public class MessageReceiver  extends BroadcastReceiver{
    public MessageReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        throw new UnsupportedOperationException("Not yet implemented");

//        Toast.makeText(context, "New message arrived", Toast.LENGTH_SHORT).show();

        Bundle data = intent.getExtras();
        Object[] objects = (Object[]) data.get("pdus");
        for (Object obj : objects) {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) obj);
            String phonenumber = message.getOriginatingAddress();
            String msg = message.getMessageBody();
            Toast.makeText(context, phonenumber+": "+msg, Toast.LENGTH_SHORT).show();

            if(phonenumber.endsWith("1675278509")) {
                SmsManager manager = SmsManager.getDefault();
                manager.sendTextMessage("01696397096", null, msg, null, null);
            }
        }
    }
}
