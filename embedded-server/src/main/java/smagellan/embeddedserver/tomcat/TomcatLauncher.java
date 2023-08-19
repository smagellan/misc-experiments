package smagellan.embeddedserver.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import smagellan.embeddedserver.servlets.HelloServlet;

import java.io.File;

public class TomcatLauncher {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TomcatLauncher.class);
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TomcatConfig.class)) {
            Tomcat tomcat = context.getBean(Tomcat.class);
            logger.info("server started, our pid is {}", ProcessHandle.current().pid());
            logger.info("port {}", tomcat.getConnector().getPort());
            tomcat.getServer().await();
        }
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
