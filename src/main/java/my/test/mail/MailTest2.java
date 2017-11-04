package my.test.mail;

import java.util.Arrays;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailTest2 {
    public static void main(String[] args) throws MessagingException {
        Properties properties = System.getProperties();
        // Setup mail server
        properties.setProperty("mail.file.host", "localhost");
        properties.setProperty("mail.transport.protocol.rfc822", "file");
        Session session = Session.getDefaultInstance(properties);
        Provider[] providers = session.getProviders();
        Arrays.stream(providers).forEach(System.err::println);

        MimeMessage message = new MimeMessage(session);


        // Set From: header field of the header.
        message.setFrom(new InternetAddress("abcd@localhost.com"));
        // Set To: header field of the header.
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("abcd2@localhost.com"));

        // Set Subject: header field
        message.setSubject("This is the Subject Line!");

        // Now set the actual message
        message.setText("This is actual message");

        Transport.send(message);
    }
}
