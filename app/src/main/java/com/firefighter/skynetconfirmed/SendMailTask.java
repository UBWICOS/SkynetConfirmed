package com.firefighter.skynetconfirmed;

import java.util.List;

//import android.app.Activity;
//import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by baynhuchim on 8/16/16.
 */
public class SendMailTask extends AsyncTask {

//    private ProgressDialog statusDialog;
//    private Activity sendMailActivity;

    public SendMailTask() {
        super();
    }

//    public SendMailTask(Activity activity) {
//        sendMailActivity = activity;
//    }

//    protected void onPreExecute() {
//        statusDialog = new ProgressDialog();
//        statusDialog.setMessage("Getting ready...");
//        statusDialog.setIndeterminate(false);
//        statusDialog.setCancelable(false);
//        statusDialog.show();
//    }

    @Override
    protected Object doInBackground(Object... args) {
        try {
            publishProgress("Processing input....");
            Gmail androidEmail = new Gmail(args[0].toString(),
                    args[1].toString(), (List) args[2], args[3].toString(),
                    args[4].toString());
            publishProgress("Preparing mail message....");
            androidEmail.createEmailMessage();
            publishProgress("Sending email....");
            androidEmail.sendEmail();
            publishProgress("Email Sent.");
        } catch (Exception e) {
            publishProgress(e.getMessage());
        }
        return null;
    }

//    @Override
//    public void onProgressUpdate(Object... values) {
//        statusDialog.setMessage(values[0].toString());
//
//    }

//    @Override
//    public void onPostExecute(Object result) {
//        statusDialog.dismiss();
//    }
}
