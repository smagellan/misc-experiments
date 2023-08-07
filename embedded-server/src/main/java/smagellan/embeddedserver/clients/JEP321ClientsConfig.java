package smagellan.embeddedserver.clients;

import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class JEP321ClientsConfig {
    public static final String WEB_CLIENT_JEP321 = "webClientJep321";

    @Bean(name = WEB_CLIENT_JEP321)
    public WebClient webClientJep321() throws NoSuchAlgorithmException, KeyManagementException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .sslContext(ClientsCommon.createSslContext())
                .build();
        return WebClient.builder()
                .clientConnector(new JdkClientHttpConnector(client))
                .build();
    }
}
