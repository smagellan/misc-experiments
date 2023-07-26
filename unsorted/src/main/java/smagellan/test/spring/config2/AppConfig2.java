package smagellan.test.spring.config2;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
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
import java.net.http.HttpClient;

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

        HttpClient clt = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .sslContext(sslContext)
                .build();
        return WebClient.builder()
                .clientConnector(new JdkClientHttpConnector(clt))
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