package smagellan.embeddedserver.clients;

import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class ClientsCommon {
    private static final TrustManager[] INSECURE_TRUST_MANAGERS = {new NoVerifyExtendedTrustManager()};
    @NotNull
    static SSLContext createSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, INSECURE_TRUST_MANAGERS, new SecureRandom());
        return sslContext;
    }
}
