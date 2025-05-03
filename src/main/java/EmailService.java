import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    private final String smtpHost;
    private final int smtpPort;
    private final String username;
    private final String password;
    private final boolean useTLS;

    public EmailService(String smtpHost, int smtpPort, String username, String password, boolean useTLS) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.useTLS = useTLS;
    }

    public void sendEmail(String toEmail, String subject, String text) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(useTLS));
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);
            LOGGER.log(Level.INFO, "Email sent successfully to: {0}", toEmail);

        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send email to: " + toEmail, e);
            throw e; // Re-throw the exception to allow the caller to handle it
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred while sending email to: " + toEmail, e);
            throw new MessagingException("An unexpected error occurred",e);
        }
    }
}