package smagellan.embeddedserver.jetty;

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.http3.server.HTTP3ServerConnectionFactory;
import org.eclipse.jetty.http3.server.HTTP3ServerConnector;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.resource.URLResourceFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

//https://webtide.com/jetty-http-3-support/
public class Http3ServerLauncher {
    public static void main(String[] args) throws Exception {
        Server server = createServer();
        server.start();
    }

    @NotNull
    private static Server createServer() {
        QueuedThreadPool serverThreads = new QueuedThreadPool();
        serverThreads.setName("server");

        Server server = new Server(serverThreads);
        server.setHandler(new MyCustomHandler());
        //server.addConnector(h2cConnector(8080, server));
        //server.addConnector(h2sConnector(8081, server));
        server.addConnector(h3Connector(8443, server));
        MBeanContainer mbeanContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        server.addBean(mbeanContainer);
        return server;
    }

    private static SslContextFactory.Server sslContextFactory() {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        URLResourceFactory factory = new URLResourceFactory();
        sslContextFactory.setKeyStoreResource(factory.newResource(Http3ServerLauncher.class.getResource("/keystore.p12")));
        sslContextFactory.setKeyStorePassword("storepwd");
        return sslContextFactory;
    }

    private static Connector h2cConnector(int port, Server server) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);
        HTTP2CServerConnectionFactory h2c = new HTTP2CServerConnectionFactory(httpConfig);
        ServerConnector connector = new ServerConnector(server, http11, h2c);
        connector.setPort(port);
        return connector;
    }

    private static Connector h2sConnector(int port, Server server) {
        //from HTTP2FromWebAppIT (jetty source code)
        SslContextFactory.Server serverTLS = sslContextFactory();
        serverTLS.setCipherComparator(new HTTP2Cipher.CipherComparator());

        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        HttpConnectionFactory h1 = new HttpConnectionFactory(httpsConfig);
        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        alpn.setDefaultProtocol(h1.getProtocol());
        SslConnectionFactory ssl = new SslConnectionFactory(serverTLS, alpn.getProtocol());
        HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpsConfig);

        ServerConnector connector = new ServerConnector(server, ssl, alpn, h2, h1);
        connector.setPort(port);
        return connector;
    }

    @NotNull
    private static HTTP3ServerConnector h3Connector(int port, Server server) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.addCustomizer(new SecureRequestCustomizer());
        httpConfig.addCustomizer(new HostHeaderCustomizer());

        HTTP3ServerConnector connector = new HTTP3ServerConnector(server, sslContextFactory(), new HTTP3ServerConnectionFactory(httpConfig));
        connector.getQuicConfiguration().setPemWorkDirectory(Path.of("/tmp/pem"));

        connector.setPort(port);
        return connector;
    }
}


class MyCustomHandler extends Handler.Abstract {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MyCustomHandler.class);
    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        logger.info("uri: {}, proto: {}, headers: {}", request.getHttpURI(), request.getConnectionMetaData().getProtocol(), request.getHeaders());
        response.write(true, ByteBuffer.wrap("Hello, world".getBytes(StandardCharsets.UTF_8)), callback);
        return true;
    }
}