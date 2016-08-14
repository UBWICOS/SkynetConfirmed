package com.firefighter.skynetconfirmed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by steven on 14/08/16.
 */
public class CallReceiver extends BroadcastReceiver {

    TelephonyManager manager;
    private MediaRecorder recorder;
    private boolean recording;
    private final PhoneStateListener phoneListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    if (recording) {
                        recorder.stop();
                        recorder.reset();
                        recorder.release();
                        recorder = null;
                        recording = false;
                    }
                    break;

                default:
                    break;

            }
        }
    };

    public CallReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        
        manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        manager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        if (extras != null) {
            String state = extras.getString(TelephonyManager.EXTRA_STATE);
            assert state != null;
            if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                try {
                    if (recorder == null && !recording) {
                        Calendar calendar = Calendar.getInstance();
                        String date = calendar.get(Calendar.DATE) + "_"
                                + (calendar.get(Calendar.MONTH) + 1) + "_"
                                + calendar.get(Calendar.YEAR);
                        String time = calendar.get(Calendar.HOUR) + "_"
                                + calendar.get(Calendar.MINUTE) + "_"
                                + calendar.get(Calendar.SECOND);

                        File file = createDirIfNotExists(date + "_" + time);

                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                        recorder.setOutputFile(file.getAbsolutePath());
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    }
                    recorder.prepare();
                    recorder.start();
                    recording = true;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private File createDirIfNotExists(String path) {
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/PhoneCallRecording");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, path + ".3gpp");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
