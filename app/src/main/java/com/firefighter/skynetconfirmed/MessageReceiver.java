package com.firefighter.skynetconfirmed;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

/**
 * Created by baynhuchim on 8/14/16.
 */
public class MessageReceiver extends BroadcastReceiver{
    public MessageReceiver() {

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        throw new UnsupportedOperationException("Not yet implemented");

        SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean adrOnly = pref.getBoolean("switch_in_address_only", false);

        SmsMessage smsMessage;

        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String myNumber = tMgr.getLine1Number();

        String destNumber = "0";

        String sourceNumber, messageBody, message;
        Bundle data = intent.getExtras();
        Object[] objects = (Object[]) data.get("pdus");
        for (Object obj : objects) {
            smsMessage = SmsMessage.createFromPdu((byte[]) obj, "3gpp");
            sourceNumber = smsMessage.getOriginatingAddress();
            messageBody = smsMessage.getMessageBody();
            if(adrOnly) message = createTextMessage(sourceNumber, myNumber, "");
            else        message = createTextMessage(sourceNumber, myNumber, messageBody);

            sendTextMessage(destNumber, message);
        }
    }

    private String createTextMessage(String from, String to, String content) {
        String msg;
        if(content.equals(""))  msg = from + " -> " + to + ".";
        else                    msg = from + " -> " + to + ": " + content;
        return msg;
    }

    public void sendTextMessage(String dest, String msg) {
        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage(dest, null, msg, null, null);
    }
}
