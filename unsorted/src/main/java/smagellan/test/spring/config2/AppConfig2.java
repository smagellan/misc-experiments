package smagellan.test.spring.config2;

import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.H2AsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
public class AppConfig2 {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AppConfig2.class);
    private static final TrustManager[] INSECURE_TRUST_MANAGERS = {new NoVerifyExtendedTrustManager()};

    @Bean
    public ComponentBeanSeven beanSeven(Environment env) {
        return new ComponentBeanSeven();
    }

    @Bean
    public WebClient webClient() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, INSECURE_TRUST_MANAGERS, new SecureRandom());

        TlsConfig tlsConfig = TlsConfig.custom()
                .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                .setHandshakeTimeout(Timeout.ofMinutes(1)).build();

        PoolingAsyncClientConnectionManager mgr = PoolingAsyncClientConnectionManagerBuilder.create()
                .setTlsStrategy(new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE))
                .setDefaultTlsConfig(tlsConfig)
                .setMaxConnTotal(20)
                .setMaxConnPerRoute(10)
                .setConnPoolPolicy(PoolReusePolicy.FIFO)
                .build();

        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setConnectionManager(mgr)
                .build();

        return WebClient.builder()
                .clientConnector(new HttpComponentsClientHttpConnector(client))
                .build();
    }
}


class NoVerifyExtendedTrustManager extends X509ExtendedTrustManager {

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] a_certificates, final String a_auth_type) {
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] a_certificates, final String a_auth_type) {
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] a_certificates, final String a_auth_type, final Socket a_socket) {
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] a_certificates, final String a_auth_type, final Socket a_socket) {
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] a_certificates, final String a_auth_type, final SSLEngine a_engine) {
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] a_certificates, final String a_auth_type, final SSLEngine a_engine) {
    }
}