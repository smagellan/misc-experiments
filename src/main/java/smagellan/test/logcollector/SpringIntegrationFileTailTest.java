package smagellan.test.logcollector;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import java.util.List;

public class SpringIntegrationFileTailTest {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpringIntegrationFileTailTest.class);

    public static void main(String[] args) throws Throwable {
        AnnotationConfigApplicationContext cfg = new AnnotationConfigApplicationContext();
        try {
            cfg.register(IntegrationConfig.class);
            cfg.registerShutdownHook();
            cfg.refresh();
            MessageChannel controlBusChannel = cfg.getBean(Constants.CONTROL_BUS + ".input", MessageChannel.class);
            List<String> fileTailers = cfg.getBean(Constants.FILE_TAILER_IDS, List.class);
            logger.info("starting file tailers");
            for (String tailerId : fileTailers) {
                controlBusChannel.send(new GenericMessage<>("@" + tailerId + ".start()"));
            }

            logger.info("waiting");
            Thread.sleep(60_000);
            logger.info("closing spring context");
            cfg.close();
        } catch (Throwable t) {
            logger.error("error creating spring context", t);
            cfg.close();
        }
    }
}
