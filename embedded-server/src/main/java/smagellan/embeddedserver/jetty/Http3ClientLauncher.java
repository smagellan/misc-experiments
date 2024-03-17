package smagellan.embeddedserver.jetty;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.http3.client.HTTP3Client;
import org.eclipse.jetty.http3.client.transport.HttpClientTransportOverHTTP3;
import org.eclipse.jetty.quic.client.ClientQuicConfiguration;
import org.slf4j.LoggerFactory;
import smagellan.embeddedserver.JettyUtils;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class Http3ClientLauncher {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Http3ClientLauncher.class);
    public static void main(String[] args) throws Exception {
        int port = 8443;

        ClientQuicConfiguration quicConfig = new ClientQuicConfiguration(JettyUtils.jettySslClientContextFactory(), Path.of("/tmp"));
        HTTP3Client h3Client = new HTTP3Client(quicConfig);
        HttpClientTransportOverHTTP3 transport = new HttpClientTransportOverHTTP3(h3Client);
        HttpClient client = new HttpClient(transport);
        try {
            h3Client.getQuicConfiguration().setSessionRecvWindow(64 * 1024 * 1024);
            h3Client.getClientConnector().setSslContextFactory(JettyUtils.jettySslClientContextFactory());

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
}
