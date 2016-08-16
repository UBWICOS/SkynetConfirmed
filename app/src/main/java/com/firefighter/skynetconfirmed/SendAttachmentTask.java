package com.firefighter.skynetconfirmed;

import android.os.AsyncTask;

import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by steven on 17/08/16.
 */
public class SendAttachmentTask extends AsyncTask {
    public SendAttachmentTask() {
        super();
    }

    @Override
    protected Object doInBackground(Object... args) {
        sendAttachment(args[0].toString(), args[1].toString(), args[2].toString(), args[3].toString(),
                args[4].toString(), args[5].toString());
        return new Object();
    }

    private void sendAttachment(final String fromAdd, final String fromPass, final String toAdd,
                                String subject, String bodyPart, String filepath) {
        String host = "smtp.gmail.com";

        Properties properties = System.getProperties();

        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.user", fromAdd);
        properties.put("mail.smtp.password", fromPass);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromAdd, fromPass);
            }
        });


        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAdd));
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(toAdd));
            message.setSubject(subject);
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(bodyPart);
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filepath);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filepath);
            multipart.addBodyPart(messageBodyPart);

            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
