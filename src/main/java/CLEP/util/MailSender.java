package CLEP.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

// TODO: Needs testing with proper email

public class MailSender {
    private final InternetAddress recipientEmail;
    private final InternetAddress senderAddress;
    private final Session session;

    public MailSender(String senderUsername, String senderPassword, InternetAddress senderAddress, int userID, Queries queries) throws SQLException {
        this.senderAddress = senderAddress;

        try (ResultSet customerEmail = queries.executeQuery("SELECT email FROM users WHERE id = ?", userID)) {
            this.recipientEmail = new InternetAddress(customerEmail.getString("email"));
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }

        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.ssl.trust", "sandbox.smtp.mailtrap.io");

        session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderUsername, senderPassword);
            }
        });
    }

    public void sendMail(String subject, String content, String ccAddresses) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(senderAddress);
            message.setRecipient(Message.RecipientType.TO, recipientEmail);
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccAddresses));
            message.setSubject(subject);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(content, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
