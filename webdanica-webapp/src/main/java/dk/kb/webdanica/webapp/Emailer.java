/*
 * Created on 18/06/2013
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package dk.kb.webdanica.webapp;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Emailer {

    /** Logging mechanism. */
    private static Logger logger = Logger.getLogger(Emailer.class.getName());

    private static Emailer emailer;

    private Properties props = new Properties();

    private Session session;

    private Emailer(String smtp_host, int smtp_port, final String username,
            final String password) {
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtp_host);
        props.put("mail.smtp.port", smtp_port);

        if (username != null && username.length() > 0 && password != null
                && password.length() > 0) {
            session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username,
                                    password);
                        }
                    });
        } else {
            session = Session.getInstance(props);
        }
    }

    public static synchronized Emailer getInstance(String smtp_host,
            int smtp_port, String username, String password) {
        if (emailer == null) {
            emailer = new Emailer(smtp_host, smtp_port, username, password);
        }
        return emailer;
    }

    public void send(String recipient, String subject, String body) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("nicl@kb.dk"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        }
    }

}
