package smagellan.embeddedserver.jetty;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.http3.client.HTTP3Client;
import org.eclipse.jetty.http3.client.transport.HttpClientTransportOverHTTP3;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

public class Http3ClientLauncher {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Http3ClientLauncher.class);
    public static void main(String[] args) throws Exception {
        int port = 8443;

        HTTP3Client h3Client = new HTTP3Client();
        HttpClientTransportOverHTTP3 transport = new HttpClientTransportOverHTTP3(h3Client);
        HttpClient client = new HttpClient(transport);
        try {
            h3Client.getQuicConfiguration().setSessionRecvWindow(64 * 1024 * 1024);
            h3Client.getClientConnector().setSslContextFactory(sslContextFactory());

            // Create and configure the HTTP/3 transport.
            client.start();

            //String uri = "https://localhost:" + 48517;
            String uri = "https://fonts.gstatic.com";
            ContentResponse r = client.newRequest(uri)
                    .timeout(5, TimeUnit.SECONDS)
                    .send();

            //ContentResponse r = client.GET("https://quic.rocks:4433/");
            logger.info("answer: " + r.getContentAsString());
        } finally {
            client.stop();
            h3Client.shutdown();
        }
        logger.info("exiting");
    }

    private static SslContextFactory.Client sslContextFactory() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = Http3ClientLauncher.class.getResourceAsStream("/keystore.p12")) {
            trustStore.load(is, "storepwd".toCharArray());
        }
        SslContextFactory.Client clientSslContextFactory = new SslContextFactory.Client();
        clientSslContextFactory.setTrustStore(trustStore);
        return clientSslContextFactory;
    }
}
