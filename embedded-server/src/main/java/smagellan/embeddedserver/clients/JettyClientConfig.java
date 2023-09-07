package smagellan.embeddedserver.clients;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.transport.HttpClientTransportDynamic;
import org.eclipse.jetty.client.transport.HttpClientConnectionFactory;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.transport.ClientConnectionFactoryOverHTTP2;
import org.eclipse.jetty.http3.client.HTTP3Client;
import org.eclipse.jetty.http3.client.transport.ClientConnectionFactoryOverHTTP3;
import org.eclipse.jetty.io.ClientConnectionFactory;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class JettyClientConfig {
    public static final String JETTY_CLIENT = "jettyClient";

    //https://webtide.com/jetty-http-3-support/
    //https://eclipse.dev/jetty/documentation/jetty-11/programming-guide/index.html#pg-client-http-transport-dynamic
    @Bean(name = JETTY_CLIENT)
    public WebClient jettyClient() throws Exception {
        HttpClient client = httpClient();

        return WebClient.builder()
                .clientConnector(new JettyClientHttpConnector(client))
                .build();
    }

    @Bean(destroyMethod = "stop", initMethod = "start")
    public HttpClient httpClient() throws Exception {
        ClientConnector connector = new ClientConnector();
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client(true);
        connector.setSslContextFactory(sslContextFactory);

        ClientConnectionFactory.Info http1 = HttpClientConnectionFactory.HTTP11;

        HTTP2Client http2Client = new HTTP2Client(connector);
        ClientConnectionFactoryOverHTTP2.HTTP2 http2 = new ClientConnectionFactoryOverHTTP2.HTTP2(http2Client);
        ClientConnectionFactoryOverHTTP3.HTTP3 http3 = http3Client(sslContextFactory);
        HttpClientTransportDynamic transport = new HttpClientTransportDynamic(connector, http1, http2, http3);

        HttpClient client = new HttpClient(transport);
        return client;
    }

    @NotNull
    private static ClientConnectionFactoryOverHTTP3.HTTP3 http3Client(SslContextFactory.Client sslContextFactory) {
        HTTP3Client http3Client = new HTTP3Client();
        http3Client.getClientConnector().setSslContextFactory(sslContextFactory);
        ClientConnectionFactoryOverHTTP3.HTTP3 http3 = new ClientConnectionFactoryOverHTTP3.HTTP3(http3Client);
        return http3;
    }
}