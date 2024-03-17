package smagellan.embeddedserver;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jetbrains.annotations.NotNull;
import smagellan.embeddedserver.jetty.Http3ClientLauncher;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class JettyUtils {
    public static SslContextFactory.Client jettySslClientContextFactory() throws Exception {
        SslContextFactory.Client clientSslContextFactory = new SslContextFactory.Client();
        clientSslContextFactory.setTrustStore(clientTrustStore());
        return clientSslContextFactory;
    }

    @NotNull
    private static KeyStore clientTrustStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = Http3ClientLauncher.class.getResourceAsStream("/keystore.p12")) {
            trustStore.load(is, "storepwd".toCharArray());
        }
        return trustStore;
    }

    public static SslContextFactory.Server jettySslServerContextFactory() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        SslContextFactory.Server clientSslContextFactory = new SslContextFactory.Server();
        clientSslContextFactory.setTrustStore(clientTrustStore());
        return clientSslContextFactory;
    }
}
