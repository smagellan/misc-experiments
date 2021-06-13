package smagellan.test.logcollector;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

public class MessageLogger {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MessageLogger.class);

    public static void logMessage(Message<?> message) {
        Object payload = message.getPayload();
        logger.info("payload: {}", payload);
        if (payload instanceof GroupedLogEvents) {
            GroupedLogEvents evts = (GroupedLogEvents) payload;
            int idx = 0;
            logger.info("GroupedLogEvents tailed files:");
            for (Map.Entry<Path, Collection<Map<String, String>>> path : evts.tailedLines().asMap().entrySet()) {
                ++idx;
                logger.info("el{}: {}({} lines)", idx, path.getKey(), path.getValue().size());
            }
            logger.info("GroupedLogEvents rolled files:");
            idx = 0;
            for (Pair<LogFileInfo, Path> pathInfo : evts.rolledFiles()) {
                ++idx;
                logger.info("el{}: {}", idx, pathInfo);
            }
        } else if (payload instanceof Collection) {
            logger.info("payload list:");
            int idx = 0;
            for (Message<?> obj : (Collection<Message<?>>) payload) {
                ++idx;
                logger.info("el{}: {}", idx, obj);
            }
        } else {
            if (payload instanceof Flux) {
                Flux<Message<?>> flux = (Flux<Message<?>>) payload;
                logger.info("subscribing to {}", flux);
                flux.collectList().subscribe((msg -> logger.info("subscribe result: {}", msg)));
            }
        }
    }
}
