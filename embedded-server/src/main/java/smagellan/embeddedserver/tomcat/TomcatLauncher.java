package smagellan.embeddedserver.tomcat;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import smagellan.embeddedserver.servlets.HelloServlet;
import smagellan.embeddedserver.tomcat.tomcat.EmbededContextConfig;
import smagellan.embeddedserver.tomcat.tomcat.EmbededStandardJarScanner;
import smagellan.embeddedserver.tomcat.tomcat.WebXmlMountListener;
import smagellan.embeddedserver.tomcat.tomcat.TomcatUtil;

import java.io.File;
import java.io.IOException;

public class TomcatLauncher {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TomcatLauncher.class);
    public static void main(String[] args) throws LifecycleException, IOException {
        Tomcat tomcat = doWebAppTest();
        tomcat.start();
        logger.info("server started, our pid is {}", ProcessHandle.current().pid());
        logger.info("port {}", tomcat.getConnector().getPort());
        tomcat.getServer().await();
    }

    @NotNull
    private static Tomcat doWebAppTest() throws IOException {
        String hostName = "localhost";
        int port = 8080;
        String contextPath = "/app";

        String tomcatBaseDir = TomcatUtil.createTempDir("tomcat", port).getAbsolutePath();
        String contextDocBase = TomcatUtil.createTempDir("tomcat-docBase", port).getAbsolutePath();

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(tomcatBaseDir);

        tomcat.setPort(port);
        tomcat.setHostname(hostName);

        Host host = tomcat.getHost();
        tomcat.getEngine().setJvmRoute("42");
        Context context = tomcat.addWebapp(host, contextPath, contextDocBase, new EmbededContextConfig());

        context.setJarScanner(new EmbededStandardJarScanner());

        ClassLoader classLoader = TomcatLauncher.class.getClassLoader();
        context.setParentClassLoader(classLoader);

        // context load WEB-INF/web.xml from classpath
        context.addLifecycleListener(new WebXmlMountListener());

        Connector connector = tomcat.getConnector();
        postConfigureConnector(connector);
        //tomcat.getService().addLifecycleListener(new AprLifecycleListener());


        Connector httpsConnector = createHttpsConnector();


        tomcat.getService().addConnector(httpsConnector);

        return tomcat;
    }

    private static void postConfigureConnector(Connector connector) {
        connector.setProperty("compression", "on");
        connector.setProperty("compressableMimeType", "text/html,text/xml,text/plain,application/javascript");
        connector.addUpgradeProtocol(new Http2Protocol());
    }

    @NotNull
    private static Connector createHttpsConnector() {
        Connector connector = new Connector("HTTP/1.1");
        connector.setSecure(true);
        connector.setScheme("https");
        connector.setProperty("SSLEnabled", "true");
        connector.setProperty("protocol", "HTTP/1.1");
        connector.setProperty("sslProtocol", "TLS");
        connector.setProperty("SSLEngine", "on");
        connector.setPort(8443);
        postConfigureConnector(connector);

        SSLHostConfig config = new SSLHostConfig();
        config.setHostName("_default_");//SSLHostConfig.DEFAULT_SSL_HOST_NAME
        SSLHostConfigCertificate cert = new SSLHostConfigCertificate(config, SSLHostConfigCertificate.Type.RSA);
        cert.setCertificateKeyFile("tls/server-key.pem");
        cert.setCertificateFile("tls/server-cert.pem");
        config.addCertificate(cert);
        connector.addSslHostConfig(config);
        return connector;
    }

    private static Tomcat doServletTest() {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        File base = new File(System.getProperty("java.io.tmpdir"));
        Context rootCtx = tomcat.addContext("/app", base.getAbsolutePath());
        Tomcat.addServlet(rootCtx, "helloServlet", new HelloServlet());
        rootCtx.addServletMappingDecoded("/date", "helloServlet");
        return tomcat;
    }
}
