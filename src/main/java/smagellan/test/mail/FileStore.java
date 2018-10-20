package smagellan.test.mail;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

public class FileStore extends Store {

    public FileStore(Session session, URLName urlname) {
        super(session, urlname);
    }

    @Override
    public Folder getDefaultFolder() throws MessagingException {
        return null;
    }

    @Override
    public Folder getFolder(String name) throws MessagingException {
        return null;
    }

    @Override
    public Folder getFolder(URLName url) throws MessagingException {
        return null;
    }
}
