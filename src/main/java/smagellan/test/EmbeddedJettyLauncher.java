package smagellan.test;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.DispatcherType;
import java.io.IOException;
import java.util.EnumSet;

public class EmbeddedJettyLauncher {

    public static final String CONTEXT_PATH = "";
    public static final String MAPPING_URL = "";
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EmbeddedJettyLauncher.class);

    private WebApplicationContext buildContext(String configLocation) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation(configLocation);
        return context;
    }

    private ServletContextHandler buildServletContextHandler(WebApplicationContext context) throws IOException {
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setErrorHandler(null);
        contextHandler.setContextPath(CONTEXT_PATH);
        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);
        contextHandler.addServlet(new ServletHolder(dispatcherServlet), MAPPING_URL);
        contextHandler.addEventListener(new ContextLoaderListener(context));

        FilterHolder filterChain = new FilterHolder(new DelegatingFilterProxy("springSecurityFilterChain", context));
        contextHandler.addFilter(filterChain, "/*", EnumSet.allOf(DispatcherType.class));

        contextHandler.setResourceBase(new ClassPathResource("webapp").getURI().toString());
        return contextHandler;
    }
}
