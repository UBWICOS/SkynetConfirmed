package com.firefighter.skynetconfirmed;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import android.text.format.DateFormat;
import java.util.Date;

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

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean adrOnly = pref.getBoolean("switch_in_address_only", false);
        boolean getTime = pref.getBoolean("switch_message_time", false);
        boolean useKeywords = pref.getBoolean("switch_use_keywords", false);
        boolean useMsgAdr = pref.getBoolean("switch_use_message_address", false);
        String[] keywords = pref.getString("keywords_text", "").split(" | ");
        String[] msgAdrs = pref.getString("message_address_text", "").split(" | ");

        SmsMessage smsMessage;

        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String myNumber = tMgr.getLine1Number();

        String destNumber = "0";

        String sourceNumber, messageBody, message;
        Date time;
        Bundle data = intent.getExtras();
        Object[] objects = (Object[]) data.get("pdus");
        for (Object obj : objects) {
            smsMessage = SmsMessage.createFromPdu((byte[]) obj, "3gpp");
            sourceNumber = smsMessage.getOriginatingAddress();
            if(getTime) time = new Date(smsMessage.getTimestampMillis());
            else        time = null;

            messageBody = smsMessage.getMessageBody();

            boolean hasKeywords = false, hasAdr = false;
            if (!useKeywords) {
                hasKeywords = true;
            } else {
                String s = messageBody.toLowerCase();
                for (String s1 : keywords) {
                    String s2 = s1.toLowerCase();
                    if (s.contains(s2)) {
                        hasKeywords = true;
                        break;
                    }
                }
            }
            if (!useMsgAdr) {
                hasAdr = true;
            } else {
                String ss = sourceNumber.toLowerCase();
                for (String s1 : msgAdrs) {
                    String s2 = s1.toLowerCase();
                    if (!s2.equals("") && ss.endsWith(s2)) {
                        hasAdr = true;
                        break;
                    }
                }
            }
            if (hasKeywords && hasAdr) {
                if (adrOnly) message = createTextMessage(sourceNumber, myNumber, "", time);
                else message = createTextMessage(sourceNumber, myNumber, messageBody, time);

                sendTextMessage(destNumber, message);
            }
        }
    }

    private String createTextMessage(String from, String to, String content, Date time) {
        String msg = from + " -> " + to;
        if(content.equals(""))  msg += ".";
        else                    msg += ": " + content;
        if(time != null)
            msg += "\nat " + DateFormat.format("HH:mm, EEE dd/MM/yyyy", time);
        return msg;
    }

    public void sendTextMessage(String dest, String msg) {
        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage(dest, null, msg, null, null);
    }
}
