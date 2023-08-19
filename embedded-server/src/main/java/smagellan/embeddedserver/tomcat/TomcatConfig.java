package smagellan.embeddedserver.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import smagellan.embeddedserver.tomcat.tomcat.EmbededContextConfig;
import smagellan.embeddedserver.tomcat.tomcat.EmbededStandardJarScanner;
import smagellan.embeddedserver.tomcat.tomcat.TomcatUtil;
import smagellan.embeddedserver.tomcat.tomcat.WebXmlMountListener;

import java.io.IOException;

@Configuration
public class TomcatConfig {
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Tomcat tomcat() throws IOException {
        String hostName = "localhost";
        int port = 8080;

        String tomcatBaseDir = TomcatUtil.createTempDir("tomcat", port).getAbsolutePath();

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(tomcatBaseDir);

        tomcat.setPort(port);
        tomcat.setHostname(hostName);

        Host host = tomcat.getHost();
        tomcat.getEngine().setJvmRoute("42");
        addWebApp(tomcat, host, "/app", TomcatUtil.createTempDir("tomcat-docBase-app", port).getAbsolutePath());

        postConfigureConnector(tomcat.getConnector());
        //tomcat.getService().addLifecycleListener(new AprLifecycleListener());
        tomcat.getService().addConnector(httpsConnector());
        return tomcat;
    }

    private void addWebApp(Tomcat tomcat, Host host, String contextPath, String contextDocBase) {
        Context context = tomcat.addWebapp(host, contextPath, contextDocBase, new EmbededContextConfig());

        context.setJarScanner(new EmbededStandardJarScanner());
        //context.setCookies(false);

        ClassLoader classLoader = TomcatLauncher.class.getClassLoader();
        context.setParentClassLoader(classLoader);

        // context load WEB-INF/web.xml from classpath
        context.addLifecycleListener(new WebXmlMountListener("WEB-INF/app"));
    }


    private void postConfigureConnector(Connector connector) {
        connector.setProperty("compression", "on");
        connector.setProperty("compressableMimeType", "text/html,text/xml,text/plain,application/javascript");
        connector.addUpgradeProtocol(new Http2Protocol());
    }



    @Bean
    public Connector httpsConnector() {
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
}
