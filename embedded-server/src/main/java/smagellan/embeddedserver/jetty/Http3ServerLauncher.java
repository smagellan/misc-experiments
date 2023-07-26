package smagellan.embeddedserver.jetty;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http3.server.HTTP3ServerConnectionFactory;
import org.eclipse.jetty.http3.server.HTTP3ServerConnector;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.jetbrains.annotations.NotNull;
import smagellan.embeddedserver.servlets.JakartaServlet;

import java.io.IOException;
import java.lang.management.ManagementFactory;

//https://webtide.com/jetty-http-3-support/
public class Http3ServerLauncher {
    public static void main(String[] args) throws Exception {
        int port = 8443;
        Server server = createServer(port);
        server.start();
    }

    @NotNull
    private static Server createServer(int port) {
        QueuedThreadPool serverThreads = new QueuedThreadPool();
        serverThreads.setName("server");

        Server server = new Server(serverThreads);
        server.setHandler(new CustomHandler());
        server.addConnector(h2cConnector(8080, server));
        SslContextFactory.Server sslContextFactory = sslContextFactory();
        server.addConnector(h3Connector(port, server, sslContextFactory));
        MBeanContainer mbeanContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        server.addBean(mbeanContainer);
        return server;
    }

    @NotNull
    private static ServletContextHandler getServletContextHandler(Server server) {
        ServletContextHandler handler = new ServletContextHandler(server,"/app", true, false);
        ServletHolder servletHolder = new ServletHolder(JakartaServlet.class);
        handler.addServlet(servletHolder, "/date");
        return handler;
    }

    private static SslContextFactory.Server sslContextFactory() {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath("keystore.p12");
        sslContextFactory.setKeyStorePassword("storepwd");
        return sslContextFactory;
    }

    @NotNull
    private static SslContextFactory.Server sslContextFactory1() {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

        sslContextFactory.setKeyStorePath("keystore.jks");
        sslContextFactory.setKeyStorePassword("12345678");
        return sslContextFactory;
    }



    private static Connector h2cConnector(int port, Server server) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        // The ConnectionFactory for HTTP/1.1.
        HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);

        // The ConnectionFactory for clear-text HTTP/2.
        HTTP2CServerConnectionFactory h2c = new HTTP2CServerConnectionFactory(httpConfig);

        // The ServerConnector instance.
        ServerConnector connector = new ServerConnector(server, http11, h2c);
        connector.setPort(port);
        return connector;
    }

    @NotNull
    private static HTTP3ServerConnector h3Connector(int port, Server server, SslContextFactory.Server sslContextFactory) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.addCustomizer(new SecureRequestCustomizer());

        // Create and configure the HTTP/3 connector.
        HTTP3ServerConnector connector = new HTTP3ServerConnector(server, sslContextFactory, new HTTP3ServerConnectionFactory());

        connector.setPort(port);
        return connector;
    }
}


class CustomHandler extends AbstractHandler {
    @Override
    public void handle(String target, Request jettyRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        jettyRequest.setHandled(true);
        response.getOutputStream().print("Hello, world");
    }
}