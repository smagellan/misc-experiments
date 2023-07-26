package smagellan.test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;

public class KeyStoreInfoDumper {
    static {
        System.setProperty("java.security.auth.debug", "certpath,provider");
        System.setProperty("java.security.debug", "certpath,provider");
        //System.setProperty("javax.net.debug", "ssl");
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static void main(String[] args) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        char[] password = "changeit".toCharArray();
        try (InputStream is = new FileInputStream(System.getProperty("java.home") + "/lib/security/cacerts")) {
            ks.load(is, password);
        }
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            Certificate cert = ks.getCertificate(alias);
            Key key = ks.getKey(alias, password);
            if (key != null) {
                System.err.println("key " + alias + "; " + key.getAlgorithm() + "; " + key.getFormat());
            }
            X509Certificate x509 = (X509Certificate)cert;
            System.err.println("cert " + alias + ": [" + FMT.format(dt(x509.getNotBefore())) + "; " + FMT.format(dt(x509.getNotAfter())) + "]; " + x509.getSigAlgName());
        }

        URL url = new URL("https://www.google.com");
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            try (InputStream is = conn.getInputStream()) {
                System.err.println(new String(is.readAllBytes()).substring(0, 100));
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static LocalDateTime dt(Date src) {
        return src.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
