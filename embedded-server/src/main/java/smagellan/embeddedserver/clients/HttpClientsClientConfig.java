package smagellan.embeddedserver.clients;

import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class HttpClientsClientConfig {
    public static final String WEB_CLIENT_APACHE = "webClientApache";
    @Bean(name = WEB_CLIENT_APACHE)
    public WebClient webClientApache() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = ClientsCommon.createSslContext();

        TlsConfig tlsConfig = TlsConfig.custom()
                .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                .setHandshakeTimeout(Timeout.ofMinutes(1)).build();

        PoolingAsyncClientConnectionManager mgr = PoolingAsyncClientConnectionManagerBuilder.create()
                .setTlsStrategy(new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE))
                .setDefaultTlsConfig(tlsConfig)
                .setMaxConnTotal(20)
                .setMaxConnPerRoute(10)
                .build();

        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setConnectionManager(mgr)
                .build();

        return WebClient.builder()
                .clientConnector(new HttpComponentsClientHttpConnector(client))
                .build();
    }
}
