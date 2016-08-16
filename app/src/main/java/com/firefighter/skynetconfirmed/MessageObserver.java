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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
        boolean useKeywords = pref.getBoolean("switch_use_keywords", false);
        boolean useMsgAdr = pref.getBoolean("switch_use_message_address", false);
        String[] keywords = pref.getString("keywords_text", "").split("|");
        String[] msgAdrs = pref.getString("message_address_text", "").split(";");
        String[] mshq_phone = pref.getString("mshq_phone_address_text", "").split(";");
        for (int i=0; i<mshq_phone.length; i++) mshq_phone[i] = mshq_phone[i].trim();
        String[] mshq_email = pref.getString("mshq_email_address_text", "").split(";");
        for (int i=0; i<mshq_email.length; i++) mshq_email[i] = mshq_email[i].trim();

        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String myNumber = tMgr.getLine1Number();

//        String destNumber = "0";
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

            boolean isDestNumber = false;
            String sourceNumber = cursor.getString(addressColumn);
            for (String destNumber : mshq_phone) {
               if (sourceNumber.equals(destNumber)) {
                   isDestNumber = true;
                   break;
               }
            }
            if (!isDestNumber) {
                if (getTime) time = new Date(cursor.getLong(dateColumn));
                else time = null;
                String messageBody = cursor.getString(bodyColumn);

                boolean hasKeywords = false, hasAdr = false;
                if (!useKeywords) {
                    hasKeywords = true;
                } else {
                    String s = messageBody.toLowerCase();
                    for (String s1 : keywords) {
                        String s2 = s1.trim().toLowerCase();
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
                        String s2 = s1.trim().toLowerCase();
                        if (!s2.equals("") && ss.endsWith(s2)) {
                            hasAdr = true;
                            break;
                        }
                    }
                }

                if (hasKeywords && hasAdr) {
                    String message;
                    if (adrOnly) message = createTextMessage(myNumber, sourceNumber, "", time);
                    else message = createTextMessage(myNumber, sourceNumber, messageBody, time);

                    for (String destNumber : mshq_phone) {
                        sendTextMessage(destNumber, message);
                    }
                    String  fromEmail = context.getString(R.string.pref_default_mshq_email),
                            fromPassword = context.getString(R.string.pref_default_mshq_email_password);
                    List<String> toEmailList = new ArrayList<>(Arrays.asList(mshq_email));
                    sendEmail(fromEmail, fromPassword, toEmailList, myNumber, sourceNumber, message, time);
                }
            }
        }
        cursor.close();
    }

    private String createTextMessage(String from, String to, String msgBody, Date time) {
        String msg = from + " -> " + to;
        if(msgBody.equals(""))  msg += ".";
        else                    msg += ": " + msgBody;
        if(time != null)
            msg += "\nat " + DateFormat.format("HH:mm, EEE dd/MM/yyyy", time);
        return msg;
    }

    public void sendTextMessage(String dest, String msg) {
        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage(dest, null, msg, null, null);
    }

    public void sendEmail (String fromEmail, String fromPassword, List<String> toEmailList, String from, String to, String msgBody, Date time) {
        String subject = "";
        if (time != null)
            subject += DateFormat.format("HH:mm, EEE dd/MM/yyyy", time);
        subject += ": " + from + " -> " + to;

        new SendMailTask().execute(fromEmail, fromPassword,
                toEmailList, subject, msgBody);
    }
}
