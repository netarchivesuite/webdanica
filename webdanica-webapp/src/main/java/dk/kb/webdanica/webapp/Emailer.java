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

    final private Properties props = new Properties();

    final private Session session;
    
    final private String fromMail;
    
    final private String mailAdmin;
    
    final private boolean dontSendMails;

    private Emailer(String smtp_host, int smtp_port, final String username,
            final String password, String mailAdmin, boolean dontSendMails) {
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtp_host);
        props.put("mail.smtp.port", smtp_port);
        this.fromMail = mailAdmin;
        this.mailAdmin = mailAdmin;
        this.dontSendMails = dontSendMails;
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
    /**
     * 
     * @param smtp_host
     * @param smtp_port
     * @param username
     * @param password
     * @param mail_admin
     * @param dontSendMails
     * @return
     */
    public static synchronized Emailer getInstance(String smtp_host,
            int smtp_port, String username, String password, String mail_admin, boolean dontSendMails) {
        if (emailer == null) {
            emailer = new Emailer(smtp_host, smtp_port, username, password, mail_admin, dontSendMails);
        }
        return emailer;
    }

    public void send(String recipient, String subject, String body) {
    	if (dontSendMails) {
    		return;
    	}
    	logger.info("Sending mail to '" + recipient + "' with subject '" + subject + "'"); 
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromMail)); 
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        }
    }
    
    public void sendAdminEmail(String subject, String body) {
		emailer.send(this.mailAdmin, subject, body);
    }
    
}
