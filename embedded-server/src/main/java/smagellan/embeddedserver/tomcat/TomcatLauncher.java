package smagellan.embeddedserver.tomcat;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
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

        return tomcat;
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
