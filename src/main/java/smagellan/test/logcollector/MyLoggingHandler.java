package smagellan.test.logcollector;

import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

class MyLoggingHandler extends AbstractMessageHandler {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MyLoggingHandler.class);

    @Override
    protected void handleMessageInternal(Message<?> message) {
        Object payload = message.getPayload();
        logger.info("payload: {}", payload);
        if (payload instanceof GroupedLogEvents) {
            GroupedLogEvents evts = (GroupedLogEvents) payload;
            int idx = 0;
            logger.info("GroupedLogEvents tailed files:");
            for (Map.Entry<Path, Collection<String>> path : evts.tailedLines().asMap().entrySet()) {
                ++idx;
                logger.info("el{}: {}({} lines)", idx, path.getKey(), path.getValue().size());
            }
            logger.info("GroupedLogEvents rolled files:");
            idx = 0;
            for (Path path : evts.rolledFiles()) {
                ++idx;
                logger.info("el{}: {}", idx, path);
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
