package smagellan.test.logcollector;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import java.util.List;

public class LogCollectorMain {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LogCollectorMain.class);

    public static void main(String[] args) throws Throwable {
        AnnotationConfigApplicationContext cfg = new AnnotationConfigApplicationContext();
        try {
            cfg.register(IntegrationConfig.class);
            cfg.registerShutdownHook();
            cfg.refresh();
            startFileTailers(cfg);
            logger.info("waiting");
            Thread.sleep(60_000);
            logger.info("closing spring context");
            cfg.close();
        } catch (Throwable t) {
            logger.error("error creating spring context", t);
            cfg.close();
        }
    }

    private static void startFileTailers(AnnotationConfigApplicationContext cfg) {
        MessageChannel controlBusChannel = cfg.getBean(Constants.CONTROL_BUS + ".input", MessageChannel.class);
        List<String> fileTailers = cfg.getBean(Constants.FILE_TAILER_IDS, List.class);
        logger.info("starting file tailers");
        for (String tailerId : fileTailers) {
            logger.info("starting {}", tailerId);
            controlBusChannel.send(new GenericMessage<>("@" + tailerId + ".start()"));
        }
    }
}
