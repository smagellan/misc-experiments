package smagellan.test;

import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.EnableEvent;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.moditect.jfrunit.events.GarbageCollection;
import org.moditect.jfrunit.events.ThreadSleep;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;


@JfrEventTest
public class JfrUnitTest {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JfrUnitTest.class);

    public JfrEvents jfrEvents = new JfrEvents();

    @Test
    @EnableEvent(GarbageCollection.EVENT_NAME)
    @EnableEvent(ThreadSleep.EVENT_NAME)
    public void doJfrTest() {
        jfrEvents.awaitEvents();
        logger.info("jfr events: {}", jfrEvents.events().collect(Collectors.toList()));
    }
}
