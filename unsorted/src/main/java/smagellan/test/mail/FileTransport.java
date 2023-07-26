package smagellan.test.mail;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

public class FileTransport extends Transport {

    public FileTransport(Session session, URLName urlname) {
        super(session, urlname);
    }

    @Override
    public void sendMessage(Message msg, Address[] addresses) throws MessagingException {
        System.err.println("FileTransport.sendMessage, subject:" + msg.getSubject());
    }

    protected boolean protocolConnect(String host, int port, String user,
            String password) throws MessagingException {
        return true;
    }
}
