package smagellan.test.mongodb;

import com.mongodb.event.ServerClosedEvent;
import com.mongodb.event.ServerDescriptionChangedEvent;
import com.mongodb.event.ServerListener;
import com.mongodb.event.ServerOpeningEvent;
import org.slf4j.LoggerFactory;

public class TracingServerListener implements ServerListener {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TracingServerListener.class);

    @Override
    public void serverOpening(ServerOpeningEvent event) {
        logger.info("serverOpening: {}", event);
    }

    @Override
    public void serverClosed(ServerClosedEvent event) {
        logger.info("serverClosed: {}", event);
    }

    @Override
    public void serverDescriptionChanged(ServerDescriptionChangedEvent event) {
        logger.info("serverDescriptionChanged: {}", event);
    }
}
