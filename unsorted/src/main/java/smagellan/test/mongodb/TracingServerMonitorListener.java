package smagellan.test.mongodb;

import com.mongodb.event.ServerHeartbeatFailedEvent;
import com.mongodb.event.ServerHeartbeatStartedEvent;
import com.mongodb.event.ServerHeartbeatSucceededEvent;
import com.mongodb.event.ServerMonitorListener;
import org.slf4j.LoggerFactory;

public class TracingServerMonitorListener implements ServerMonitorListener {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TracingServerMonitorListener.class);

    @Override
    public void serverHearbeatStarted(ServerHeartbeatStartedEvent event) {
        logger.info("serverHearbeatStarted: {}", event);
    }

    @Override
    public void serverHeartbeatSucceeded(ServerHeartbeatSucceededEvent event) {
        logger.info("serverHeartbeatSucceeded: {}", event);
    }

    @Override
    public void serverHeartbeatFailed(ServerHeartbeatFailedEvent event) {
        logger.info("serverHeartbeatFailed: {}", event);
    }
}
