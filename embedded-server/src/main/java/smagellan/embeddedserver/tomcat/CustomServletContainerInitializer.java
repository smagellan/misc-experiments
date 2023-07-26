package smagellan.embeddedserver.tomcat;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import javax.servlet.http.HttpServlet;
import java.util.Set;

@HandlesTypes({ HttpServlet.class })
public class CustomServletContainerInitializer implements ServletContainerInitializer {
    private static final Log log = LogFactory.getLog(CustomServletContainerInitializer.class);
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext ctx) throws ServletException {
        log.info("CustomServletContainerInitializer.onStartup");
        if (set != null) {
            for (Class<?> c : set) {
                log.info("servlet: " + c.getName());
            }
        }
    }

}
