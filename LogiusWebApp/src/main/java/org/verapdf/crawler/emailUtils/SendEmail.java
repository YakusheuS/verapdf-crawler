package org.verapdf.crawler.emailUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.verapdf.crawler.domain.email.EmailServer;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class SendEmail {
    private static Logger logger = LoggerFactory.getLogger(SendEmail.class);
    public static void send(String targetEmail, String subject, String text, EmailServer emailServer) {

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.port", emailServer.port);
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", emailServer.host);
        Session session = Session.getDefaultInstance(properties, new GMailAuthenticator(emailServer.user,emailServer.password));

        try {
            Transport transport = session.getTransport();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailServer.address));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(targetEmail));
            message.setSubject(subject);
            message.setText(text);

            transport.connect();
            Transport.send(message);
            transport.close();

        }catch (MessagingException mex) {
            logger.error("Email sending error", mex);
        }
    }
}